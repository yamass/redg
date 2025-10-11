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

import de.yamass.redg.generator.testutil.DatabaseType;
import de.yamass.redg.generator.extractor.conveniencesetterprovider.DefaultConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.testutil.*;
import de.yamass.redg.models.ColumnModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.sql.Types;

import static de.yamass.redg.generator.testutil.DatabaseType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DbTest()
class ColumnExtractorTest2 {

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	private Catalog catalog;

	private ColumnExtractor columnExtractor;

	@BeforeEach
	void init() throws Exception {
		Assertions.assertNotNull(dataSource);
		catalog = DatabaseManager.crawlDatabase(dataSource, DatabaseTypeTestUtil.testSchemaInclusionRule(databaseType), new IncludeAll());
		columnExtractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(), new DefaultExplicitAttributeDecider(), new DefaultConvenienceSetterProvider());
	}

	@TestTemplate
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-int.sql")
	void testInt() throws Exception {
		ColumnModel model = columnExtractor.extractColumnModel(ExtractorTestUtil.createDataTypeLookup(catalog), getColumnFromCatalog("t", "c"));

		assertThat(model.getDbTableName()).isEqualToIgnoringCase("t");
		assertThat(model.getDbFullTableName()).matches("(?i).*\\.t$");
		assertThat(model.getJavaPropertyName()).isEqualToIgnoringCase("c");
		assertThat(model.getDbName()).isEqualToIgnoringCase("c");
		assertThat(model.getSqlTypeName()).isEqualToIgnoringCase(databaseType.getDataTypesLookup().getIntegerType());
		assertThat(model.getJavaTypeName()).isEqualToIgnoringCase("java.lang.Integer");
		assertThat(model.getJavaTypeAsClass()).isEqualTo(Integer.class);
		assertThat(model.getSqlTypeInt()).isEqualTo(Types.INTEGER);
	}

	@TestTemplate
	@Databases({H2, POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-varchar.sql")
	void testVarchar() throws Exception {
		ColumnModel model = columnExtractor.extractColumnModel(ExtractorTestUtil.createDataTypeLookup(catalog), getColumnFromCatalog("t", "c"));

		assertThat(model.getDbTableName()).isEqualToIgnoringCase("t");
		assertThat(model.getDbFullTableName()).matches("(?i).*\\.t$");
		assertThat(model.getJavaPropertyName()).isEqualToIgnoringCase("c");
		assertThat(model.getDbName()).isEqualToIgnoringCase("c");
		assertThat(model.getSqlTypeName()).isEqualToIgnoringCase(databaseType.getDataTypesLookup().getVarcharType());
		assertThat(model.getJavaTypeName()).isEqualToIgnoringCase("java.lang.String");
		assertThat(model.getJavaTypeAsClass()).isEqualTo(String.class);
		assertThat(model.getSqlTypeInt()).isEqualTo(Types.VARCHAR);
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/generator/extractor/ColumnExtractorTest-enum.sql")
	void testEnum_postgres() throws Exception {
		ColumnModel model = columnExtractor.extractColumnModel(ExtractorTestUtil.createDataTypeLookup(catalog), getColumnFromCatalog("t", "c"));

		assertThat(model.getDbTableName()).isEqualToIgnoringCase("t");
		assertThat(model.getDbFullTableName()).matches("(?i).*\\.t$");
		assertThat(model.getJavaPropertyName()).isEqualToIgnoringCase("c");
		assertThat(model.getDbName()).isEqualToIgnoringCase("c");
		assertThat(model.getSqlTypeName()).isEqualToIgnoringCase("my_enum");
		assertThat(model.getJavaTypeName()).isEqualToIgnoringCase("java.lang.String");  // TODO change to generated enum
		assertThat(model.getJavaTypeAsClass()).isEqualTo(String.class); // TODO can't change this to a (not yet existing) generated class. Do we actually need this?
		assertThat(model.getSqlTypeInt()).isEqualTo(Types.VARCHAR); // TODO change to actual type?

		assertThat(model.getDataType().getName()).isEqualToIgnoringCase("my_enum");
		assertThat(model.getDataType().isEnumerated()).isTrue();
		assertThat(model.getDataType().getEnumValues()).containsExactly("A", "B", "C");
	}

	private Column getColumnFromCatalog(String tableName, String columnName) throws Exception {
		Schema s = catalog.getSchemas().stream()
				.filter(schema -> {
					String name = schema.getName() != null ? schema.getName() : schema.getCatalogName();
					return name.equalsIgnoreCase(DatabaseTypeTestUtil.testSchemaName(databaseType));
				})
				.findFirst()
				.orElseThrow();

		Table t = catalog.getTables(s).stream()
				.filter(tbale -> tbale.getName().equalsIgnoreCase(tableName))
				.findFirst()
				.orElseThrow();

		return t.getColumns().stream()
				.filter(column -> column.getName().equalsIgnoreCase(columnName))
				.findFirst()
				.orElseThrow();
	}
}
