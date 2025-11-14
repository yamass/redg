package de.yamass.redg.schema.inspector;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DataType;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.DefaultNumberDataType;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.SchemaInspectionResult;
import de.yamass.redg.schema.model.Table;
import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.Udt;
import de.yamass.redg.schema.vendor.SchemaInfoRetriever;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SchemaInspector {
	private final DatabaseType databaseType;
	private final DataSource dataSource;

	public SchemaInspector(DatabaseType databaseType, DataSource dataSource) {
		this.databaseType = databaseType;
		this.dataSource = dataSource;
	}

	public SchemaInspectionResult inspectSchema(String schema) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			return inspectSchema(connection, schema, SchemaInfoRetriever.byDatabaseType(databaseType));
		}
	}

	private SchemaInspectionResult inspectSchema(Connection connection, String schema, SchemaInfoRetriever schemaInfoRetriever) throws SQLException {
		DatabaseMetaData metadata = connection.getMetaData();
		Map<QualifiedTableName, TableBuilder> tableBuilders = discoverTables(metadata, schema);
		Map<QualifiedTableName, Set<String>> uniqueColumnNames = collectUniqueColumns(metadata, tableBuilders.keySet());
		Map<QualifiedTableName, List<String>> primaryKeyColumns = collectPrimaryKeyColumns(metadata, tableBuilders.keySet());
		String vendor = metadata.getDatabaseProductName() != null ? metadata.getDatabaseProductName() : "";

		for (TableBuilder builder : tableBuilders.values()) {
			loadColumns(metadata, builder, uniqueColumnNames.getOrDefault(builder.key(), Collections.emptySet()));
			builder.setPrimaryKeyColumnNames(primaryKeyColumns.getOrDefault(builder.key(), Collections.emptyList()));
			builder.initializeTable();
		}

		List<ForeignKeySpec> foreignKeySpecs = collectForeignKeySpecs(metadata, tableBuilders.keySet());
		buildForeignKeys(foreignKeySpecs, tableBuilders);

		List<Table> tables = new ArrayList<>();
		for (TableBuilder builder : tableBuilders.values()) {
			tables.add(builder.table());
		}

		List<Constraint> constraints = schemaInfoRetriever.getConstraints(connection, schema);
		List<Udt> udts = schemaInfoRetriever.getUdts(connection, schema);

		return new SchemaInspectionResult(tables, constraints, udts);
	}

	private static Map<QualifiedTableName, TableBuilder> discoverTables(DatabaseMetaData metadata, String schema) throws SQLException {
		Map<QualifiedTableName, TableBuilder> tables = new LinkedHashMap<>();
		try (ResultSet rs = metadata.getTables(null, schema, "%", new String[]{"TABLE"})) {
			while (rs.next()) {
				String tableSchema = defaultIfBlank(rs.getString("TABLE_SCHEM"), schema);
				String tableName = rs.getString("TABLE_NAME");
				if (tableName == null) {
					continue;
				}
				QualifiedTableName key = new QualifiedTableName(tableSchema, tableName);
				tables.put(key, new TableBuilder(key));
			}
		}
		return tables;
	}

	private static Map<QualifiedTableName, List<String>> collectPrimaryKeyColumns(DatabaseMetaData metadata, Collection<QualifiedTableName> qualifiedTableNames) throws SQLException {
		Map<QualifiedTableName, List<String>> primaryKeyColumns = new LinkedHashMap<>();
		for (QualifiedTableName qTableName : qualifiedTableNames) {
			List<String> pkColumns = new ArrayList<>();
			Map<Short, String> orderedPkColumns = new LinkedHashMap<>();
			try (ResultSet pk = metadata.getPrimaryKeys(null, qTableName.schema(), qTableName.name())) {
				while (pk.next()) {
					Short keySeq = pk.getShort("KEY_SEQ");
					String columnName = pk.getString("COLUMN_NAME");
					if (columnName != null && keySeq != null) {
						orderedPkColumns.put(keySeq, columnName);
					}
				}
			}
			// Sort by KEY_SEQ to maintain the correct order
			orderedPkColumns.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(entry -> pkColumns.add(entry.getValue()));
			primaryKeyColumns.put(qTableName, pkColumns);
		}
		return primaryKeyColumns;
	}

	private static Map<QualifiedTableName, Set<String>> collectUniqueColumns(DatabaseMetaData metadata, Collection<QualifiedTableName> qualifiedTableNames) throws SQLException {
		Map<QualifiedTableName, Set<String>> uniqueColumns = new LinkedHashMap<>();
		for (QualifiedTableName qTableName : qualifiedTableNames) {
			Set<String> columns = new LinkedHashSet<>();
			List<String> primaryKeyColumns = new ArrayList<>();
			try (ResultSet pk = metadata.getPrimaryKeys(null, qTableName.schema(), qTableName.name())) {
				while (pk.next()) {
					String columnName = pk.getString("COLUMN_NAME");
					if (columnName != null) {
						primaryKeyColumns.add(columnName);
					}
				}
			}
			if (primaryKeyColumns.size() == 1) {
				columns.add(primaryKeyColumns.get(0));
			}

			Map<String, List<String>> indexColumns = new LinkedHashMap<>();
			try (ResultSet idx = metadata.getIndexInfo(null, qTableName.schema(), qTableName.name(), true, true)) {
				while (idx.next()) {
					String columnName = idx.getString("COLUMN_NAME");
					String indexName = idx.getString("INDEX_NAME");
					if (columnName == null || indexName == null) {
						continue;
					}
					indexColumns.computeIfAbsent(indexName, ignored -> new ArrayList<>()).add(columnName);
				}
			}
			for (List<String> indexColumnList : indexColumns.values()) {
				if (indexColumnList.size() == 1) {
					columns.add(indexColumnList.get(0));
				}
			}
			uniqueColumns.put(qTableName, columns);
		}
		return uniqueColumns;
	}

	private static void loadColumns(DatabaseMetaData metadata, TableBuilder builder, Set<String> uniqueColumns) throws SQLException {
		try (ResultSet cols = metadata.getColumns(null, builder.key().schema(), builder.key().name(), "%")) {
			while (cols.next()) {
				String columnName = cols.getString("COLUMN_NAME");
				if (columnName == null) {
					continue;
				}
				boolean nullable = "YES".equalsIgnoreCase(cols.getString("IS_NULLABLE"));
				boolean unique = uniqueColumns.contains(columnName);
				DataType dataType = buildDataType(cols);
				builder.addColumn(new Column(columnName, dataType, nullable, unique));
			}
		}
	}

	private static DataType buildDataType(ResultSet columnMetadata) throws SQLException {
		int jdbcTypeId = columnMetadata.getInt("DATA_TYPE");
		Optional<JDBCType> jdbcType = resolveJdbcTypeName(jdbcTypeId);
		int typeId = columnMetadata.getInt("SOURCE_DATA_TYPE");
		String typeName = columnMetadata.getString("TYPE_NAME");
		boolean autoIncrementable = "YES".equalsIgnoreCase(columnMetadata.getString("IS_AUTOINCREMENT"));

		if (isNumericType(jdbcTypeId)) {
			int precision = Optional.ofNullable(getInteger(columnMetadata, "COLUMN_SIZE")).orElse(0);
			int scale = Optional.ofNullable(getInteger(columnMetadata, "DECIMAL_DIGITS")).orElse(0);
			boolean fixed = jdbcTypeId == Types.DECIMAL || jdbcTypeId == Types.NUMERIC;
			boolean unsigned = typeName != null && typeName.toLowerCase(Locale.ROOT).contains("unsigned");
			return new DefaultNumberDataType(
					typeName,
					jdbcType.orElse(null),
					typeId,
					null,
					autoIncrementable,
					scale,
					0,
					precision,
					fixed,
					unsigned
			);
		}
		return new DefaultDataType(
				typeName,
				jdbcType.orElse(null),
				typeId,
				null,
				autoIncrementable
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

	private static List<ForeignKeySpec> collectForeignKeySpecs(DatabaseMetaData metadata, Collection<QualifiedTableName> qualifiedTableNames) throws SQLException {
		List<ForeignKeySpec> specs = new ArrayList<>();
		for (QualifiedTableName key : qualifiedTableNames) {
			Map<String, ForeignKeySpec> byName = new LinkedHashMap<>();
			try (ResultSet fk = metadata.getImportedKeys(null, key.schema(), key.name())) {
				while (fk.next()) {
					String fkName = fk.getString("FK_NAME");
					if (fkName == null || fkName.isBlank()) {
						fkName = key.name() + "_fk_" + fk.getShort("KEY_SEQ");
					}
					String targetSchema = defaultIfBlank(fk.getString("PKTABLE_SCHEM"),
							defaultIfBlank(fk.getString("PKTABLE_CAT"), key.schema()));
					QualifiedTableName targetKey = new QualifiedTableName(targetSchema, fk.getString("PKTABLE_NAME"));
					ForeignKeySpec spec = byName.computeIfAbsent(fkName, ignored -> new ForeignKeySpec(key, targetKey));
					short sequence = fk.getShort("KEY_SEQ");
					spec.addColumnPair(sequence, fk.getString("FKCOLUMN_NAME"), fk.getString("PKCOLUMN_NAME"));
				}
			}
			specs.addAll(byName.values());
		}
		return specs;
	}

	private static void buildForeignKeys(List<ForeignKeySpec> specs, Map<QualifiedTableName, TableBuilder> tables) {
		for (ForeignKeySpec spec : specs) {
			TableBuilder sourceBuilder = tables.get(spec.source());
			TableBuilder targetBuilder = tables.get(spec.target());
			if (sourceBuilder == null || targetBuilder == null) {
				continue;
			}
			List<ForeignKeyColumn> columns = new ArrayList<>();
			for (ColumnPair pair : spec.columnPairs()) {
				Column source = sourceBuilder.column(pair.sourceColumn());
				Column target = targetBuilder.column(pair.targetColumn());
				if (source != null && target != null) {
					columns.add(new ForeignKeyColumn(source, target));
				}
			}
			if (columns.isEmpty()) {
				continue;
			}
			ForeignKey foreignKey = new ForeignKey(sourceBuilder.table(), targetBuilder.table(), List.copyOf(columns));
			sourceBuilder.addOutgoingForeignKey(foreignKey);
			targetBuilder.addIncomingForeignKey(foreignKey);
		}
	}

	private static Integer getInteger(ResultSet rs, String columnLabel) throws SQLException {
		int value = rs.getInt(columnLabel);
		return rs.wasNull() ? null : value;
	}

	private static String defaultIfBlank(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value;
	}

}