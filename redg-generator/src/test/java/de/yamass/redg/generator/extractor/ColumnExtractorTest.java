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
import de.yamass.redg.generator.extractor.conveniencesetterprovider.DefaultConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.testutil.DatabaseTestUtil;
import de.yamass.redg.models.ColumnModel;
import org.junit.BeforeClass;
import org.junit.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.File;

import static org.junit.Assert.*;


public class ColumnExtractorTest {

    private static Catalog catalog;

    @BeforeClass
    public static void setUp() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cet", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        catalog = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(catalog);
    }

    @Test
    public void testColumnExtraction() throws Exception {
        Column column = extractColumnFromDemoDb("DEMO_USER", "ID");

        ColumnExtractor extractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
                new DefaultExplicitAttributeDecider(), new DefaultConvenienceSetterProvider());
        ColumnModel model = extractor.extractColumnModel(column);

        assertEquals("id", model.getName());
        assertEquals("ID", model.getDbName());
        assertEquals("DEMO_USER", model.getDbTableName());
        assertEquals("NUMERIC", model.getSqlType());
        assertEquals("java.math.BigDecimal", model.getJavaTypeName());
        assertTrue(model.isNotNull());
    }

    @Test
    public void testExtractColumnModelForExpliciteAttribute() throws Exception {
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
                }, new DefaultConvenienceSetterProvider());
        ColumnModel columnModel = extractor.extractColumnModel(column);

        assertEquals("DTYPE", columnModel.getDbName());
        assertTrue(columnModel.isExplicitAttribute());
    }

    @Test
    public void testExtractColumnModelForKeywordColumn() throws Exception {
        Column column = extractColumnFromDemoDb("DEMO_USER", "DAY_TS");

        ColumnExtractor extractor = new ColumnExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
                new DefaultExplicitAttributeDecider(), new DefaultConvenienceSetterProvider());
        ColumnModel model = extractor.extractColumnModel(column);

        assertEquals("DAY_TS", model.getDbName());
    }

    private Column extractColumnFromDemoDb(String tableName, String columnName) throws Exception {
        Schema s = catalog.lookupSchema("\"RT-CET\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = catalog.lookupTable(s, tableName).orElse(null);
        assertNotNull(t);
        Column c = t.lookupColumn(columnName).orElse(null);
        assertNotNull(c);

        return c;
    }
}
