package de.yamass.redg.schema.vendor;

import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.Udt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MariaDbSchemaInfoRetrieverImpl implements SchemaInfoRetriever {

	@Override
	public List<Constraint> getConstraints(Connection connection, String schema) throws SQLException {
		return List.of(); // TODO
	}

	@Override
	public List<Udt> getUdts(Connection connection, String schema) throws SQLException {
		return List.of(); // TODO
	}

	@Override
	public int getArrayDimensions(Connection connection, String schema, String tableName, String columnName) throws SQLException {
		return 0; // TODO
	}
}
