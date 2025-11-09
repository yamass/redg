package de.yamass.redg.schema.vendor;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.Udt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SchemaInfoRetriever {

	public static SchemaInfoRetriever byDatabaseType(DatabaseType databaseType) throws SQLException {
		return switch (databaseType) {
			case POSTGRES -> new PostgresSchemaInfoRetrieverImpl();
			case MARIADB -> new MariaDbSchemaInfoRetrieverImpl();
			case H2 -> new H2SchemaInfoRetrieverImpl();
			case GENERIC -> new GenericSchemaInfoRetrieverImpl();
		};
	}

	List<Constraint> getConstraints(Connection connection, String schema) throws SQLException;

	List<Udt> getUdts(Connection connection, String schema) throws SQLException;

	/**
	 * Gets the number of array dimensions for a column type.
	 * @param connection the database connection
	 * @param schema the schema name
	 * @param tableName the table name
	 * @param columnName the column name
	 * @return the number of array dimensions, or 0 if not an array or cannot be determined
	 * @throws SQLException if a database error occurs
	 */
	int getArrayDimensions(Connection connection, String schema, String tableName, String columnName) throws SQLException;

	/**
	 * Gets the enum values for an enumerated type column.
	 * @param connection the database connection
	 * @param schema the schema name
	 * @param tableName the table name (may be null if not available)
	 * @param columnName the column name (may be null if not available)
	 * @param typeName the type name (e.g., enum type name or column type definition)
	 * @return the list of enum values, or an empty list if not an enum or cannot be determined
	 * @throws SQLException if a database error occurs
	 */
	default java.util.List<String> getEnumValues(Connection connection, String schema, String tableName, String columnName, String typeName) throws SQLException {
		return java.util.Collections.emptyList();
	}

}
