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
			case MARIADB -> throw new UnsupportedOperationException("Not yet implemented.");
			case H2 -> throw new UnsupportedOperationException("Not yet implemented.");
			case GENERIC -> throw new UnsupportedOperationException("Not yet implemented.");
		};
	}

	List<Constraint> getConstraints(Connection connection, String schema) throws SQLException;

	List<Udt> getUdts(Connection connection, String schema) throws SQLException;

}
