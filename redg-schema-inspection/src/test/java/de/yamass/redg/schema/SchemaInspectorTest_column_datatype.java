package de.yamass.redg.schema;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.schema.inspector.SchemaInspector;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DataType;
import de.yamass.redg.schema.model.NumberDataType;
import de.yamass.redg.schema.model.SchemaInspectionResult;
import de.yamass.redg.schema.model.Table;
import de.yamass.redg.testing.DbContext;
import de.yamass.redg.testing.DbTest;
import de.yamass.redg.testing.Databases;
import de.yamass.redg.testing.Scripts;
import org.junit.jupiter.api.TestTemplate;

import javax.sql.DataSource;
import java.sql.JDBCType;
import java.sql.SQLException;

import static de.yamass.redg.DatabaseType.POSTGRES;
import static org.assertj.core.api.Assertions.assertThat;

@DbTest
class SchemaInspectorTest_column_datatype {

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-numeric.sql")
	void extractsNumericDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_numeric_table");

		assertJdbcType(table, "column_bigint", JDBCType.BIGINT);
		assertJdbcType(table, "column_integer", JDBCType.INTEGER);
		assertJdbcType(table, "column_smallint", JDBCType.SMALLINT);
		assertJdbcType(table, "column_tinyint", JDBCType.SMALLINT); // PostgreSQL maps TINYINT to SMALLINT
		assertJdbcType(table, "column_numeric", JDBCType.NUMERIC);
		assertJdbcType(table, "column_real", JDBCType.REAL);
		assertJdbcType(table, "column_double", JDBCType.DOUBLE);
		assertJdbcType(table, "column_float", JDBCType.DOUBLE); // PostgreSQL maps FLOAT to DOUBLE
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-decimal.sql")
	void extractsDecimalDataType() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_decimal_table");

		assertJdbcType(table, "column_decimal", JDBCType.NUMERIC); // PostgreSQL maps DECIMAL to NUMERIC

		// Verify decimal type has NumberDataType with correct precision and scale
		Column decimalColumn = table.findColumnOrThrow("column_decimal");
		assertThat(decimalColumn.type()).isInstanceOf(NumberDataType.class);
		NumberDataType decimalType = (NumberDataType) decimalColumn.type();
		assertThat(decimalType.getPrecision()).isEqualTo(10);
		assertThat(decimalType.getMaximumScale()).isEqualTo(2);
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-string.sql")
	void extractsStringDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_string_table");

		assertJdbcType(table, "column_varchar", JDBCType.VARCHAR);
		assertJdbcType(table, "column_char", JDBCType.CHAR);
		assertJdbcType(table, "column_text", JDBCType.VARCHAR); // PostgreSQL TEXT maps to VARCHAR
		assertJdbcType(table, "column_clob", JDBCType.VARCHAR); // PostgreSQL CLOB maps to VARCHAR
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-binary.sql")
	void extractsBinaryDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_binary_table");

		// PostgreSQL BYTEA maps to BINARY in JDBC
		// All BYTEA columns map to the same JDBC type regardless of the column name
		Column binaryColumn = table.findColumnOrThrow("column_binary");
		Column varbinaryColumn = table.findColumnOrThrow("column_varbinary");
		Column blobColumn = table.findColumnOrThrow("column_blob");
		Column longvarbinaryColumn = table.findColumnOrThrow("column_longvarbinary");

		// Verify all BYTEA columns have a JDBC type (they all map to BINARY)
		assertThat(binaryColumn.type().getJdbcType()).isPresent();
		assertThat(varbinaryColumn.type().getJdbcType()).isPresent();
		assertThat(blobColumn.type().getJdbcType()).isPresent();
		assertThat(longvarbinaryColumn.type().getJdbcType()).isPresent();

		// All should map to BINARY (PostgreSQL's BYTEA maps to JDBC BINARY)
		JDBCType expectedType = binaryColumn.type().getJdbcType().orElseThrow();
		assertThat(varbinaryColumn.type().getJdbcType()).contains(expectedType);
		assertThat(blobColumn.type().getJdbcType()).contains(expectedType);
		assertThat(longvarbinaryColumn.type().getJdbcType()).contains(expectedType);
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-date-time.sql")
	void extractsDateTimeDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_date_time_table");

		assertJdbcType(table, "column_date", JDBCType.DATE);
		assertJdbcType(table, "column_time", JDBCType.TIME);
		assertJdbcType(table, "column_timestamp", JDBCType.TIMESTAMP);
		assertJdbcType(table, "column_timestamptz", JDBCType.TIMESTAMP); // PostgreSQL maps TIMESTAMP WITH TIME ZONE to TIMESTAMP
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-boolean.sql")
	void extractsBooleanDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_boolean_table");

		assertJdbcType(table, "column_boolean", JDBCType.BIT); // PostgreSQL BOOLEAN maps to BIT
		assertJdbcType(table, "column_bit", JDBCType.BIT);
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-other.sql")
	void extractsOtherDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_other_table");

		// PostgreSQL-specific types may not map directly to standard JDBC types
		// We verify they are extracted, but JDBCType may be null or OTHER
		Column arrayColumn = table.findColumnOrThrow("column_array");
		DataType arrayType = arrayColumn.type();
		assertThat(arrayType.getName()).isEqualTo("_int4"); // PostgreSQL array type name

		Column jsonColumn = table.findColumnOrThrow("column_json");
		DataType jsonType = jsonColumn.type();
		assertThat(jsonType.getName()).isEqualTo("json");

		Column jsonbColumn = table.findColumnOrThrow("column_jsonb");
		DataType jsonbType = jsonbColumn.type();
		assertThat(jsonbType.getName()).isEqualTo("jsonb");

		Column uuidColumn = table.findColumnOrThrow("column_uuid");
		DataType uuidType = uuidColumn.type();
		assertThat(uuidType.getName()).isEqualTo("uuid");

		Column xmlColumn = table.findColumnOrThrow("column_xml");
		DataType xmlType = xmlColumn.type();
		assertThat(xmlType.getName()).isEqualTo("xml");
	}

	private SchemaInspectionResult inspectPublicSchema() throws SQLException {
		SchemaInspector schemaInspector = new SchemaInspector(databaseType, dataSource);
		return schemaInspector.inspectSchema("public");
	}

	private void assertJdbcType(Table table, String columnName, JDBCType expectedJdbcType) {
		Column column = table.findColumnOrThrow(columnName);
		DataType dataType = column.type();
		assertThat(dataType.getJdbcType())
				.as("JDBC type for column %s", columnName)
				.isPresent()
				.contains(expectedJdbcType);
	}
}

