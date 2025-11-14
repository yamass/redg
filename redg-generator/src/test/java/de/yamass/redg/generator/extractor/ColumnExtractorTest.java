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

import de.yamass.redg.generator.Helpers;
import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.util.ScriptRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.File;

import static de.yamass.redg.generator.extractor.ExtractorTestUtil.createDataTypeLookup;


class ColumnExtractorTest {

	private static Catalog catalog;

	@BeforeAll
	public static void setUp() throws Exception {
		DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cet", "", "");
		Assertions.assertNotNull(dataSource);
		File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
		Assertions.assertNotNull(tempFile);
		ScriptRunner.executeScripts(dataSource, tempFile);
		catalog = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
		Assertions.assertNotNull(catalog);
	}

	@Test
	void testColumnExtraction() throws Exception {
		Column column = extractColumnFromDemoDb("DEMO_USER", "ID");

		ColumnExtractor extractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
				new DefaultExplicitAttributeDecider(), ConvenienceSetterProvider.NONE);
		ColumnModel model = extractor.extractColumnModel(createDataTypeLookup(catalog), column);

		Assertions.assertEquals("id", model.getJavaPropertyName());
		Assertions.assertEquals("ID", model.getDbName());
		Assertions.assertEquals("DEMO_USER", model.getDbTableName());
		Assertions.assertEquals("NUMERIC", model.getSqlTypeName());
		Assertions.assertEquals("java.math.BigDecimal", model.getJavaTypeName());
		Assertions.assertTrue(model.isNotNull());
	}

	@Test
	void testExtractColumnModelForExpliciteAttribute() throws Exception {
		Column column = extractColumnFromDemoDb("DEMO_USER", "DTYPE");

		ColumnExtractor extractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
				new ExplicitAttributeDecider() {
					@Override
					public boolean isExplicitAttribute(final Column column) {
						return column.getName().equals("DTYPE");
					}

					@Override
					public boolean isExplicitForeignKey(final ForeignKey foreignKey) {
						return false;
					}
				}, ConvenienceSetterProvider.NONE);
		ColumnModel columnModel = extractor.extractColumnModel(createDataTypeLookup(catalog), column);

		Assertions.assertEquals("DTYPE", columnModel.getDbName());
		Assertions.assertTrue(columnModel.isExplicitAttribute());
	}

	@Test
	void testExtractColumnModelForKeywordColumn() throws Exception {
		Column column = extractColumnFromDemoDb("DEMO_USER", "DAY_TS");

		ColumnExtractor extractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
				new DefaultExplicitAttributeDecider(), ConvenienceSetterProvider.NONE);
		ColumnModel model = extractor.extractColumnModel(createDataTypeLookup(catalog), column);

		Assertions.assertEquals("DAY_TS", model.getDbName());
	}

	private Column extractColumnFromDemoDb(String tableName, String columnName) throws Exception {
		Schema s = catalog.lookupSchema("\"RT-CET\".PUBLIC").orElse(null);
		Assertions.assertNotNull(s);
		Table t = catalog.lookupTable(s, tableName).orElse(null);
		Assertions.assertNotNull(t);
		Column c = t.lookupColumn(columnName).orElse(null);
		Assertions.assertNotNull(c);

		return c;
	}

}
