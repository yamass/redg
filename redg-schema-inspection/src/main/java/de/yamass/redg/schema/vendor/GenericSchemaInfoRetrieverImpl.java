package de.yamass.redg.schema.vendor;

import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.Udt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Generic SchemaInfoRetriever implementation for databases that don't have a specific implementation.
 * This implementation returns empty results and relies on standard JDBC metadata for basic schema information.
 */
public class GenericSchemaInfoRetrieverImpl implements SchemaInfoRetriever {

	@Override
	public List<Constraint> getConstraints(Connection connection, String schema) throws SQLException {
		// For generic databases, we rely on standard JDBC metadata which doesn't provide
		// detailed constraint information beyond primary keys and foreign keys (which are
		// already extracted by SchemaInspector using standard JDBC metadata).
		return Collections.emptyList();
	}

	@Override
	public List<Udt> getUdts(Connection connection, String schema) throws SQLException {
		return Collections.emptyList();
	}

	@Override
	public int getArrayDimensions(Connection connection, String schema, String tableName, String columnName) throws SQLException {
		return 0; // Cannot determine array dimensions for generic databases
	}

}

