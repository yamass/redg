/*
 * Copyright Yann Massard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.generator.extractor;

import de.yamass.redg.generator.testutil.*;
import de.yamass.redg.models.DataTypeModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;

import javax.sql.DataSource;
import java.sql.JDBCType;
import java.util.function.Predicate;

import static de.yamass.redg.generator.testutil.DatabaseType.MARIADB;
import static de.yamass.redg.generator.testutil.DatabaseType.POSTGRES;
import static de.yamass.redg.generator.utils.SchemaCrawlerUtil.lookupColumnDataType;
import static org.assertj.core.api.Assertions.assertThat;

@DbTest()
class DataTypeExtractorTest {

	private static final Logger LOG = LoggerFactory.getLogger(DataTypeExtractorTest.class);

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	@Mock
	private DataTypeLookup dataTypeLookupMock;

	private Catalog catalog;

	private DataTypeExtractor dataTypeExtractor;


	@BeforeEach
	void init() throws Exception {
		Assertions.assertNotNull(dataSource);
		catalog = DatabaseManager.crawlDatabase(dataSource, DatabaseTypeTestUtil.testSchemaInclusionRule(databaseType), new IncludeAll());
		dataTypeExtractor = new DataTypeExtractor();
	}

	@TestTemplate
	@Databases(DatabaseType.POSTGRES)
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-int.sql")
	void testInt_postgres() throws Exception {
		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, lookupColumnDataType(catalog, databaseType, "t", "c").orElseThrow());

		assertThat(dataTypeModel.getName()).isEqualToIgnoringCase("int4");
		assertThat(dataTypeModel.getDatabaseSpecificTypeName()).isEqualTo("int4");
		assertThat(dataTypeModel.getLocalTypeName()).isNull(); // !?
		assertThat(dataTypeModel.getJavaSqlTypeName()).matches(JDBCType.INTEGER.getName());
		assertThat(dataTypeModel.getVendor()).isEqualTo(JDBCType.INTEGER.getVendor());
		assertThat(dataTypeModel.getVendorTypeNumber()).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
		assertThat(dataTypeModel.getBaseType()).isNull();
		assertThat(dataTypeModel.getEnumValues()).isEmpty();
		assertThat(dataTypeModel.getLiteralPrefix()).isNullOrEmpty();
		assertThat(dataTypeModel.getLiteralSuffix()).isNullOrEmpty();
		assertThat(dataTypeModel.getMaximumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getMinimumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getNumPrecisionRadix()).isEqualTo(10);
		assertThat(dataTypeModel.getPrecision()).isEqualTo(0);
		assertThat(dataTypeModel.getDefaultJavaClass()).isEqualTo(Integer.class);
		assertThat(dataTypeModel.isAutoIncrementable()).isFalse();
		assertThat(dataTypeModel.isEnumerated()).isFalse();
		assertThat(dataTypeModel.isFixedPrecisionScale()).isFalse();
		assertThat(dataTypeModel.isNullable()).isTrue();
		assertThat(dataTypeModel.isUnsigned()).isFalse();
	}

	@TestTemplate
	@Databases(DatabaseType.MARIADB)
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-int.sql")
	void testInt_mariadb() throws Exception {
		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, lookupColumnDataType(catalog, databaseType, "t", "c").orElseThrow());

		assertThat(dataTypeModel.getName()).isEqualToIgnoringCase("INT");
		assertThat(dataTypeModel.getDatabaseSpecificTypeName()).isEqualTo("INT");
		assertThat(dataTypeModel.getLocalTypeName()).isEqualTo("INT");
		assertThat(dataTypeModel.getJavaSqlTypeName()).matches(JDBCType.INTEGER.getName());
		assertThat(dataTypeModel.getVendor()).isEqualTo(JDBCType.INTEGER.getVendor());
		assertThat(dataTypeModel.getVendorTypeNumber()).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
		assertThat(dataTypeModel.getBaseType()).isNull();
		assertThat(dataTypeModel.getEnumValues()).isEmpty();
		assertThat(dataTypeModel.getLiteralPrefix()).isNullOrEmpty();
		assertThat(dataTypeModel.getLiteralSuffix()).isNullOrEmpty();
		assertThat(dataTypeModel.getMaximumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getMinimumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getNumPrecisionRadix()).isEqualTo(10);
		assertThat(dataTypeModel.getPrecision()).isEqualTo(10); // well, yeah...
		assertThat(dataTypeModel.getDefaultJavaClass()).isEqualTo(Integer.class);
		assertThat(dataTypeModel.isAutoIncrementable()).isTrue();
		assertThat(dataTypeModel.isEnumerated()).isFalse();
		assertThat(dataTypeModel.isFixedPrecisionScale()).isFalse();
		assertThat(dataTypeModel.isNullable()).isTrue();
		assertThat(dataTypeModel.isUnsigned()).isFalse();
	}

	@TestTemplate
	@Databases(DatabaseType.H2)
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-int.sql")
	void testInt_h2() throws Exception {
		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, lookupColumnDataType(catalog, databaseType, "t", "c").orElseThrow());

		assertThat(dataTypeModel.getName()).isEqualToIgnoringCase("INTEGER");
		assertThat(dataTypeModel.getDatabaseSpecificTypeName()).isEqualTo("INTEGER");
		assertThat(dataTypeModel.getLocalTypeName()).isEqualTo("INTEGER");
		assertThat(dataTypeModel.getJavaSqlTypeName()).matches(JDBCType.INTEGER.getName());
		assertThat(dataTypeModel.getVendor()).isEqualTo(JDBCType.INTEGER.getVendor());
		assertThat(dataTypeModel.getVendorTypeNumber()).isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
		assertThat(dataTypeModel.getBaseType()).isNull();
		assertThat(dataTypeModel.getEnumValues()).isEmpty();
		assertThat(dataTypeModel.getLiteralPrefix()).isNullOrEmpty();
		assertThat(dataTypeModel.getLiteralSuffix()).isNullOrEmpty();
		assertThat(dataTypeModel.getMaximumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getMinimumScale()).isEqualTo(0);
		assertThat(dataTypeModel.getNumPrecisionRadix()).isEqualTo(2);
		assertThat(dataTypeModel.getPrecision()).isEqualTo(32); // well, yeah...
		assertThat(dataTypeModel.getDefaultJavaClass()).isEqualTo(Integer.class);
		assertThat(dataTypeModel.isAutoIncrementable()).isTrue();
		assertThat(dataTypeModel.isEnumerated()).isFalse();
		assertThat(dataTypeModel.isFixedPrecisionScale()).isFalse();
		assertThat(dataTypeModel.isNullable()).isTrue();
		assertThat(dataTypeModel.isUnsigned()).isFalse();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-enum.sql")
	void testEnum_postgres() throws Exception {
		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, lookupColumnDataType(catalog, databaseType, "t", "c").orElseThrow());

//		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, getDataType(cdt -> cdt.getName().equalsIgnoreCase("my_enum")));

		assertThat(dataTypeModel.getName()).isEqualToIgnoringCase("my_enum");
		assertThat(dataTypeModel.getDatabaseSpecificTypeName()).isEqualTo("my_enum");
		assertThat(dataTypeModel.getLocalTypeName()).isNull();
		assertThat(dataTypeModel.getJavaSqlTypeName()).matches(JDBCType.VARCHAR.getName());
		assertThat(dataTypeModel.getVendor()).isEqualTo(JDBCType.VARCHAR.getVendor());
		assertThat(dataTypeModel.getVendorTypeNumber()).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
		assertThat(dataTypeModel.getBaseType()).isNull();
		assertThat(dataTypeModel.getEnumValues()).containsExactly("A", "B", "C");
		assertThat(dataTypeModel.getLiteralPrefix()).isEqualTo("'");
		assertThat(dataTypeModel.getLiteralSuffix()).isEqualTo("'");
		assertThat(dataTypeModel.getDefaultJavaClass()).isEqualTo(String.class);
		assertThat(dataTypeModel.isAutoIncrementable()).isFalse();
		assertThat(dataTypeModel.isEnumerated()).isTrue();
		assertThat(dataTypeModel.isNullable()).isTrue();
	}

	@TestTemplate
	@Databases({MARIADB})
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-enum.sql")
	void testEnum_mariadb() throws Exception {
		DataTypeModel dataTypeModel = dataTypeExtractor.extractDataType(dataTypeLookupMock, lookupColumnDataType(catalog, databaseType, "t", "c").orElseThrow());

		assertThat(dataTypeModel.getName()).isEqualToIgnoringCase("ENUM");
		assertThat(dataTypeModel.getDatabaseSpecificTypeName()).isEqualTo("ENUM");
		assertThat(dataTypeModel.getLocalTypeName()).isEqualTo("ENUM");
		assertThat(dataTypeModel.getJavaSqlTypeName()).matches(JDBCType.VARCHAR.getName());
		assertThat(dataTypeModel.getVendor()).isEqualTo(JDBCType.VARCHAR.getVendor());
		assertThat(dataTypeModel.getVendorTypeNumber()).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
		assertThat(dataTypeModel.getBaseType()).isNull();
		assertThat(dataTypeModel.getEnumValues()).containsExactly("A", "B", "C");
		assertThat(dataTypeModel.getLiteralPrefix()).isEqualTo("'");
		assertThat(dataTypeModel.getLiteralSuffix()).isEqualTo("'");
		assertThat(dataTypeModel.getDefaultJavaClass()).isEqualTo(String.class);
		assertThat(dataTypeModel.isAutoIncrementable()).isFalse();
		assertThat(dataTypeModel.isEnumerated()).isTrue();
		assertThat(dataTypeModel.isNullable()).isTrue();
	}

	private ColumnDataType getDataType(JDBCType jdbcType) throws Exception {
		return getDataType(cdt -> JDBCType.valueOf(cdt.getJavaSqlType().getVendorTypeNumber()) == jdbcType);
	}


	private ColumnDataType getDataType(Predicate<ColumnDataType> predicate) throws Exception {
		ColumnDataType columnDataType = catalog.getColumnDataTypes().stream()
				.filter(predicate)
				.findFirst()
				.orElseThrow();
		LOG.info("Using column data type: {}", columnDataType);
		return columnDataType;
	}
}
