package de.yamass.redg.schema;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.schema.vendor.SchemaInfoRetriever;

import javax.sql.DataSource;
import java.sql.*;

public class SchemaInspector {
	private final DatabaseType databaseType;
	private final DataSource dataSource;

	public SchemaInspector(DatabaseType databaseType, DataSource dataSource) {
		this.databaseType = databaseType;
		this.dataSource = dataSource;
	}

	public void inspectSchema(String schema) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			inspectSchema(schema, connection, SchemaInfoRetriever.byDatabaseType(databaseType));
		}

	}

	private void inspectSchema(String schema, Connection connection, SchemaInfoRetriever schemaInfoRetriever) throws SQLException {
		DatabaseMetaData dbmd = connection.getMetaData();

		try (ResultSet tables = dbmd.getTables(null, schema, "%", new String[]{"TABLE"})) {
			while (tables.next()) {
				String tableName = tables.getString("TABLE_NAME");
				System.out.println("Table: " + tableName);

				// Columns
				try (ResultSet cols = dbmd.getColumns(null, schema, tableName, "%")) {
					while (cols.next()) {
						System.out.printf("  Column: %-20s %s%n",
								cols.getString("COLUMN_NAME"),
								cols.getString("TYPE_NAME"));
					}
				}

				// Primary keys
				try (ResultSet pk = dbmd.getPrimaryKeys(null, schema, tableName)) {
					while (pk.next()) {
						System.out.println("  PK: " + pk.getString("COLUMN_NAME"));
					}
				}

				// Foreign keys
				try (ResultSet fk = dbmd.getImportedKeys(null, schema, tableName)) {
					while (fk.next()) {
						System.out.printf("  FK: %s → %s(%s)%n",
								fk.getString("FKCOLUMN_NAME"),
								fk.getString("PKTABLE_NAME"),
								fk.getString("PKCOLUMN_NAME"));
					}
				}

				// Indexes
				try (ResultSet idx = dbmd.getIndexInfo(null, schema, tableName, false, false)) {
					while (idx.next()) {
						System.out.printf("  Index: %s unique=%s%n",
								idx.getString("INDEX_NAME"),
								!idx.getBoolean("NON_UNIQUE"));
					}
				}
			}

			schemaInfoRetriever.getConstraints(connection, schema);
			schemaInfoRetriever.getUdts(connection, schema);
		}
	}
}