package de.yamass.redg.schema;

import de.yamass.redg.testing.*;
import de.yamass.redg.DatabaseType;
import org.junit.jupiter.api.TestTemplate;

import javax.sql.DataSource;

import java.sql.SQLException;

import static de.yamass.redg.DatabaseType.*;

@DbTest
class SchemaInspectorTest {

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/SchemaInspectorTest-table.sql")
	void x() throws SQLException {
		SchemaInspector schemaInspector = new SchemaInspector(databaseType, dataSource);
		schemaInspector.inspectSchema("public");
	}
}