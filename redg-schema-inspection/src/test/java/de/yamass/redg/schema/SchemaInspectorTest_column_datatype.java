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

import static de.yamass.redg.DatabaseType.MARIADB;
import static de.yamass.redg.DatabaseType.POSTGRES;
import static org.assertj.core.api.Assertions.assertThat;

@DbTest
class SchemaInspectorTest_column_datatype {

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-numeric.sql")
	void extractsNumericDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_numeric_table");

		assertJdbcType(table, "column_bigint", JDBCType.BIGINT);
		assertJdbcType(table, "column_integer", JDBCType.INTEGER);
		assertJdbcType(table, "column_smallint", JDBCType.SMALLINT);
		// PostgreSQL maps TINYINT to SMALLINT, MariaDB has native TINYINT
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_tinyint", JDBCType.SMALLINT);
		} else {
			assertJdbcType(table, "column_tinyint", JDBCType.TINYINT);
		}
		// PostgreSQL maps NUMERIC to NUMERIC, MariaDB maps NUMERIC to DECIMAL
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_numeric", JDBCType.NUMERIC);
		} else {
			assertJdbcType(table, "column_numeric", JDBCType.DECIMAL);
		}
		// PostgreSQL maps REAL to REAL, MariaDB maps REAL to DOUBLE
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_real", JDBCType.REAL);
		} else {
			assertJdbcType(table, "column_real", JDBCType.DOUBLE);
		}
		assertJdbcType(table, "column_double", JDBCType.DOUBLE);
		// PostgreSQL maps FLOAT to DOUBLE, MariaDB maps FLOAT to REAL
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_float", JDBCType.DOUBLE);
		} else {
			assertJdbcType(table, "column_float", JDBCType.REAL);
		}
	}

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-decimal.sql")
	void extractsDecimalDataType() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_decimal_table");

		// PostgreSQL maps DECIMAL to NUMERIC, MariaDB maps DECIMAL to DECIMAL
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_decimal", JDBCType.NUMERIC);
		} else {
			assertJdbcType(table, "column_decimal", JDBCType.DECIMAL);
		}

		// Verify decimal type has NumberDataType with correct precision and scale
		Column decimalColumn = table.findColumnOrThrow("column_decimal");
		assertThat(decimalColumn.type()).isInstanceOf(NumberDataType.class);
		NumberDataType decimalType = (NumberDataType) decimalColumn.type();
		assertThat(decimalType.getPrecision()).isEqualTo(10);
		assertThat(decimalType.getMaximumScale()).isEqualTo(2);
	}

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-string.sql")
	void extractsStringDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_string_table");

		assertJdbcType(table, "column_varchar", JDBCType.VARCHAR);
		assertJdbcType(table, "column_char", JDBCType.CHAR);
		// PostgreSQL maps TEXT to VARCHAR, MariaDB maps TEXT to LONGVARCHAR
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_text", JDBCType.VARCHAR);
			assertJdbcType(table, "column_clob", JDBCType.VARCHAR);
		} else {
			assertJdbcType(table, "column_text", JDBCType.LONGVARCHAR);
			assertJdbcType(table, "column_clob", JDBCType.LONGVARCHAR);
		}
	}

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-binary.sql")
	void extractsBinaryDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_binary_table");

		Column binaryColumn = table.findColumnOrThrow("column_binary");
		Column varbinaryColumn = table.findColumnOrThrow("column_varbinary");
		Column blobColumn = table.findColumnOrThrow("column_blob");
		Column longvarbinaryColumn = table.findColumnOrThrow("column_longvarbinary");

		// Verify all binary columns have a JDBC type
		assertThat(binaryColumn.type().getJdbcType()).isPresent();
		assertThat(varbinaryColumn.type().getJdbcType()).isPresent();
		assertThat(blobColumn.type().getJdbcType()).isPresent();
		assertThat(longvarbinaryColumn.type().getJdbcType()).isPresent();

		// PostgreSQL BYTEA maps to BINARY, MariaDB VARBINARY/BLOB map to VARBINARY/BLOB
		if (databaseType == POSTGRES) {
			// All BYTEA columns map to BINARY in PostgreSQL
			JDBCType expectedType = binaryColumn.type().getJdbcType().orElseThrow();
			assertThat(varbinaryColumn.type().getJdbcType()).contains(expectedType);
			assertThat(blobColumn.type().getJdbcType()).contains(expectedType);
			assertThat(longvarbinaryColumn.type().getJdbcType()).contains(expectedType);
		} else {
			// MariaDB has more specific mappings
			assertJdbcType(table, "column_binary", JDBCType.VARBINARY);
			assertJdbcType(table, "column_varbinary", JDBCType.VARBINARY);
			// MariaDB maps BLOB to LONGVARBINARY, not BLOB
			assertJdbcType(table, "column_blob", JDBCType.LONGVARBINARY);
			assertJdbcType(table, "column_longvarbinary", JDBCType.LONGVARBINARY);
		}
	}

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-date-time.sql")
	void extractsDateTimeDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_date_time_table");

		assertJdbcType(table, "column_date", JDBCType.DATE);
		assertJdbcType(table, "column_time", JDBCType.TIME);
		assertJdbcType(table, "column_timestamp", JDBCType.TIMESTAMP);
		// Both databases map timestamp with time zone to TIMESTAMP in JDBC
		assertJdbcType(table, "column_timestamptz", JDBCType.TIMESTAMP);
	}

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-boolean.sql")
	void extractsBooleanDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_boolean_table");

		// PostgreSQL maps BOOLEAN to BIT, MariaDB maps both BOOLEAN and BIT to BOOLEAN
		if (databaseType == POSTGRES) {
			assertJdbcType(table, "column_boolean", JDBCType.BIT);
			assertJdbcType(table, "column_bit", JDBCType.BIT);
		} else {
			assertJdbcType(table, "column_boolean", JDBCType.BOOLEAN);
			assertJdbcType(table, "column_bit", JDBCType.BIT);
		}
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-array.sql")
	void extractsArrayDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_array_table");

		// Test integer array
		Column integerArrayColumn = table.findColumnOrThrow("column_integer_array");
		DataType integerArrayType = integerArrayColumn.type();
		assertThat(integerArrayType.getName()).isEqualTo("_int4");
		assertThat(integerArrayType.getArrayDimensions()).isEqualTo(1);
		assertThat(integerArrayType.isArray()).isTrue();
		assertThat(integerArrayType.getBaseType()).isNotNull();
		assertThat(integerArrayType.getBaseType().getName()).isEqualTo("int4");
		assertThat(integerArrayType.getBaseType().getArrayDimensions()).isEqualTo(0);
		assertThat(integerArrayType.getBaseType().isArray()).isFalse();
		assertThat(integerArrayType.getBaseType().getJdbcType()).isPresent();
		assertThat(integerArrayType.getBaseType().getJdbcType().get()).isEqualTo(JDBCType.INTEGER);

		// Test decimal array
		Column decimalArrayColumn = table.findColumnOrThrow("column_decimal_array");
		DataType decimalArrayType = decimalArrayColumn.type();
		assertThat(decimalArrayType.getName()).isEqualTo("_numeric");
		assertThat(decimalArrayType.getArrayDimensions()).isEqualTo(1);
		assertThat(decimalArrayType.isArray()).isTrue();
		NumberDataType decimalBaseType = (NumberDataType) decimalArrayType.getBaseType();
		assertThat(decimalBaseType).isNotNull();
		assertThat(decimalBaseType.getArrayDimensions()).isEqualTo(0);
		assertThat(decimalBaseType.isArray()).isFalse();
		assertThat(decimalBaseType).isInstanceOf(NumberDataType.class);
		assertThat(decimalBaseType.getName()).isEqualTo("numeric");
		assertThat(decimalBaseType.getPrecision()).isEqualTo(10);
		assertThat(decimalBaseType.getMaximumScale()).isEqualTo(2);
		assertThat(decimalBaseType.getJdbcType()).isPresent();
		assertThat(decimalBaseType.getJdbcType().get()).isEqualTo(JDBCType.NUMERIC);

		// Test text array
		Column textArrayColumn = table.findColumnOrThrow("column_text_array");
		DataType textArrayType = textArrayColumn.type();
		assertThat(textArrayType.getName()).isEqualTo("_text");
		assertThat(textArrayType.getArrayDimensions()).isEqualTo(1);
		assertThat(textArrayType.isArray()).isTrue();
		assertThat(textArrayType.getBaseType()).isNotNull();
		assertThat(textArrayType.getBaseType().getName()).isEqualTo("text");
		assertThat(textArrayType.getBaseType().getArrayDimensions()).isEqualTo(0);
		assertThat(textArrayType.getBaseType().isArray()).isFalse();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-array-multidimensional.sql")
	void extractsMultiDimensionalArrayDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_array_multidimensional_table");

		// Test two-dimensional array
		Column twoDimArrayColumn = table.findColumnOrThrow("column_two_dimensional_array");
		DataType twoDimArrayType = twoDimArrayColumn.type();
		assertThat(twoDimArrayType.getArrayDimensions()).isEqualTo(2);
		assertThat(twoDimArrayType.isArray()).isTrue();
		assertThat(twoDimArrayType.getBaseType()).isNotNull();
		assertThat(twoDimArrayType.getBaseType().getArrayDimensions()).isEqualTo(0);
		assertThat(twoDimArrayType.getBaseType().isArray()).isFalse();

		// Test three-dimensional array
		Column threeDimArrayColumn = table.findColumnOrThrow("column_three_dimensional_array");
		DataType threeDimArrayType = threeDimArrayColumn.type();
		assertThat(threeDimArrayType.getArrayDimensions()).isEqualTo(3);
		assertThat(threeDimArrayType.isArray()).isTrue();
		assertThat(threeDimArrayType.getBaseType()).isNotNull();
		assertThat(threeDimArrayType.getBaseType().getArrayDimensions()).isEqualTo(0);
		assertThat(threeDimArrayType.getBaseType().isArray()).isFalse();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-datatype-other.sql")
	void extractsOtherDataTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();
		Table table = result.findTableOrThrow("public", "column_datatype_other_table");

		// PostgreSQL-specific types may not map directly to standard JDBC types
		// We verify they are extracted, but JDBCType may be null or OTHER
		Column jsonColumn = table.findColumnOrThrow("column_json");
		DataType jsonType = jsonColumn.type();
		assertThat(jsonType.getName()).isEqualTo("json");
		assertThat(jsonType.getArrayDimensions()).isEqualTo(0);
		assertThat(jsonType.isArray()).isFalse();
		assertThat(jsonType.getBaseType()).isNull();

		Column jsonbColumn = table.findColumnOrThrow("column_jsonb");
		DataType jsonbType = jsonbColumn.type();
		assertThat(jsonbType.getName()).isEqualTo("jsonb");
		assertThat(jsonbType.getArrayDimensions()).isEqualTo(0);
		assertThat(jsonbType.isArray()).isFalse();
		assertThat(jsonbType.getBaseType()).isNull();

		Column uuidColumn = table.findColumnOrThrow("column_uuid");
		DataType uuidType = uuidColumn.type();
		assertThat(uuidType.getName()).isEqualTo("uuid");
		assertThat(uuidType.getArrayDimensions()).isEqualTo(0);
		assertThat(uuidType.isArray()).isFalse();
		assertThat(uuidType.getBaseType()).isNull();

		Column xmlColumn = table.findColumnOrThrow("column_xml");
		DataType xmlType = xmlColumn.type();
		assertThat(xmlType.getName()).isEqualTo("xml");
		assertThat(xmlType.getArrayDimensions()).isEqualTo(0);
		assertThat(xmlType.isArray()).isFalse();
		assertThat(xmlType.getBaseType()).isNull();
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

