package de.yamass.redg.schema.vendor;

import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.ConstraintType;
import de.yamass.redg.schema.model.DataType;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.Udt;
import de.yamass.redg.schema.model.UdtField;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PostgresSchemaInfoRetrieverImpl implements SchemaInfoRetriever {

	@Override
	public List<Constraint> getConstraints(Connection connection, String schema) throws SQLException {
		String constraintsQuery = """
				SELECT c.conname, c.contype, pg_get_constraintdef(c.oid) AS def
				FROM pg_constraint c
				JOIN pg_namespace n ON n.oid = c.connamespace
				WHERE n.nspname = ?
				""";
		try (PreparedStatement ps = connection.prepareStatement(constraintsQuery)) {
			ps.setString(1, schema);

			List<Constraint> constraints = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String def = rs.getString("def");
					boolean partial = def != null && def.toLowerCase().contains("where");
					ConstraintType type = ConstraintType.fromDatabaseCode(rs.getString("contype"));
					constraints.add(new Constraint(
							schema,
							rs.getString("conname"),
							type,
							def,
							partial
					));
				}
			}
			return constraints;
		}
	}

	@Override
	public List<Udt> getUdts(Connection connection, String schema) throws SQLException {
		DatabaseMetaData metadata = connection.getMetaData();
		String udtQuery = """
				SELECT typname, typtype, typcategory, typrelid
				FROM pg_type t
				JOIN pg_namespace n ON n.oid = t.typnamespace
				WHERE n.nspname = ?
				""";
		List<Udt> udts = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(udtQuery)) {
			ps.setString(1, schema);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String typname = rs.getString("typname");
					String typtype = rs.getString("typtype");
					String typcategory = rs.getString("typcategory");
					long typrelid = rs.getLong("typrelid");
					
					// For composite types (typtype = 'c'), get the fields
					List<UdtField> fields = Collections.emptyList();
					if ("c".equals(typtype) && typrelid != 0) {
						fields = getCompositeTypeFields(connection, metadata, schema, typrelid);
					}
					
					udts.add(new Udt(
							schema,
							typname,
							typtype,
							typcategory,
							fields
					));
				}
			}
		}
		return udts;
	}
	
	private List<UdtField> getCompositeTypeFields(Connection connection, DatabaseMetaData metadata, String schema, long typrelid) throws SQLException {
		// Query pg_attribute to get the fields of a composite type
		// attrelid points to the composite type's class (from typrelid)
		// attnum > 0 excludes system columns
		String fieldsQuery = """
				SELECT a.attname, a.atttypid, a.atttypmod, t.typname AS type_name
				FROM pg_attribute a
				JOIN pg_type t ON t.oid = a.atttypid
				WHERE a.attrelid = ?
				    AND a.attnum > 0
				    AND NOT a.attisdropped
				ORDER BY a.attnum
				""";
		List<UdtField> fields = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(fieldsQuery)) {
			ps.setLong(1, typrelid);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String fieldName = rs.getString("attname");
					long atttypid = rs.getLong("atttypid");
					int atttypmod = rs.getInt("atttypmod");
					String typeName = rs.getString("type_name");
					
					DataType dataType = buildDataTypeFromTypeOid(connection, metadata, atttypid, atttypmod, typeName, schema);
					fields.add(new UdtField(fieldName, dataType));
				}
			}
		}
		return fields;
	}
	
	private DataType buildDataTypeFromTypeOid(Connection connection, DatabaseMetaData metadata, long typeOid, int typeMod, String typeName, String schema) throws SQLException {
		// Query pg_type to get more information about the type
		String typeQuery = """
				SELECT typname, typtype, oid
				FROM pg_type
				WHERE oid = ?
				""";
		
		Integer jdbcTypeId = null;
		Optional<JDBCType> jdbcType = Optional.empty();
		String resolvedTypeName = typeName;
		
		try (PreparedStatement ps = connection.prepareStatement(typeQuery)) {
			ps.setLong(1, typeOid);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					resolvedTypeName = rs.getString("typname");
				}
			}
		}
		
		// Try to find JDBC type from DatabaseMetaData.getTypeInfo()
		try (ResultSet typeInfo = metadata.getTypeInfo()) {
			while (typeInfo.next()) {
				String typeInfoName = typeInfo.getString("TYPE_NAME");
				if (resolvedTypeName != null && resolvedTypeName.equalsIgnoreCase(typeInfoName)) {
					jdbcTypeId = typeInfo.getInt("DATA_TYPE");
					jdbcType = resolveJdbcTypeName(jdbcTypeId);
					break;
				}
			}
		}
		
		// If not found in type info, try to get from PostgreSQL's type mapping
		if (jdbcTypeId == null) {
			// Query pg_type to get the JDBC type mapping
			// For standard types, we can map from PostgreSQL type names
			jdbcTypeId = mapPostgresTypeToJdbcType(resolvedTypeName);
			if (jdbcTypeId != null) {
				jdbcType = resolveJdbcTypeName(jdbcTypeId);
			}
		}
		
		// Build DataType - check if numeric type
		if (jdbcTypeId != null && isNumericType(jdbcTypeId)) {
			// Extract precision and scale from typeMod if available
			int precision = 0;
			int scale = 0;
			if (typeMod >= 0) {
				// typeMod format: (precision << 16) | scale for numeric types
				precision = (typeMod >> 16) & 0xFFFF;
				scale = typeMod & 0xFFFF;
			}
			boolean fixed = jdbcTypeId == Types.DECIMAL || jdbcTypeId == Types.NUMERIC;
			boolean unsigned = resolvedTypeName != null && resolvedTypeName.toLowerCase(Locale.ROOT).contains("unsigned");
			return new DefaultDataType(
					resolvedTypeName,
					jdbcType.orElse(null),
					jdbcTypeId,
					null,
					false,
					0,
					scale,
					0,
					precision,
					fixed,
					unsigned
			);
		}
		
		// Get enum values if this is an enum type
		List<String> enumValues = Collections.emptyList();
		if (resolvedTypeName != null) {
			try {
				enumValues = getEnumValues(connection, schema, null, null, resolvedTypeName);
			} catch (SQLException e) {
				// Ignore if enum values can't be retrieved
			}
		}
		
		return new DefaultDataType(
				resolvedTypeName,
				jdbcType.orElse(null),
				jdbcTypeId,
				null,
				false,
				0,
				0,
				0,
				0,
				false,
				false,
				enumValues
		);
	}
	
	private static boolean isNumericType(int jdbcType) {
		return switch (jdbcType) {
			case Types.BIGINT, Types.BIT, Types.DECIMAL, Types.DOUBLE, Types.FLOAT,
					Types.INTEGER, Types.NUMERIC, Types.REAL, Types.SMALLINT, Types.TINYINT -> true;
			default -> false;
		};
	}
	
	private static Optional<JDBCType> resolveJdbcTypeName(int jdbcTypeId) {
		try {
			return Optional.of(JDBCType.valueOf(jdbcTypeId));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
	
	private static Integer mapPostgresTypeToJdbcType(String postgresTypeName) {
		if (postgresTypeName == null) {
			return null;
		}
		String lower = postgresTypeName.toLowerCase(Locale.ROOT);
		return switch (lower) {
			case "int2", "smallint" -> Types.SMALLINT;
			case "int4", "integer", "int" -> Types.INTEGER;
			case "int8", "bigint" -> Types.BIGINT;
			case "numeric", "decimal" -> Types.NUMERIC;
			case "real", "float4" -> Types.REAL;
			case "double precision", "float8", "float" -> Types.DOUBLE;
			case "text", "varchar", "char", "character", "character varying" -> Types.VARCHAR;
			case "boolean", "bool" -> Types.BOOLEAN;
			case "date" -> Types.DATE;
			case "time" -> Types.TIME;
			case "timestamp", "timestamp without time zone" -> Types.TIMESTAMP;
			case "timestamp with time zone", "timestamptz" -> Types.TIMESTAMP_WITH_TIMEZONE;
			case "bytea" -> Types.BINARY;
			default -> null;
		};
	}

	@Override
	public int getArrayDimensions(Connection connection, String schema, String tableName, String columnName) throws SQLException {
		// Query PostgreSQL system catalogs to get array dimensions
		// For arrays, we count the number of '_' prefixes in the type name
		// or query pg_attribute.attndims which gives the number of dimensions
		String dimensionsQuery = """
				SELECT attndims
				FROM pg_attribute a
				JOIN pg_class c ON c.oid = a.attrelid
				JOIN pg_namespace n ON n.oid = c.relnamespace
				WHERE n.nspname = ? AND c.relname = ? AND a.attname = ?
				""";
		try (PreparedStatement ps = connection.prepareStatement(dimensionsQuery)) {
			ps.setString(1, schema);
			ps.setString(2, tableName);
			ps.setString(3, columnName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("attndims");
				}
			}
		}
		return 0;
	}

	@Override
	public List<String> getEnumValues(Connection connection, String schema, String tableName, String columnName, String typeName) throws SQLException {
		// Query PostgreSQL system catalogs to get enum values
		// pg_enum contains the enum values, ordered by oid (which reflects creation order)
		String enumQuery = """
				SELECT e.enumlabel
				FROM pg_enum e
				JOIN pg_type t ON t.oid = e.enumtypid
				JOIN pg_namespace n ON n.oid = t.typnamespace
				WHERE n.nspname = ? AND t.typname = ?
				ORDER BY e.oid
				""";
		List<String> enumValues = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(enumQuery)) {
			ps.setString(1, schema);
			ps.setString(2, typeName);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					enumValues.add(rs.getString("enumlabel"));
				}
			}
		}
		return enumValues;
	}
}
