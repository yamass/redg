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
import de.yamass.redg.generator.exceptions.RedGGenerationException;
import de.yamass.redg.generator.testutil.DatabaseTestUtil;
import de.yamass.redg.models.ForeignKeyModel;
import de.yamass.redg.models.TableModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class TableExtractorTest {

    @Test
    void testExtractTable() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-te", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executeScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        Schema s = db.lookupSchema("\"RT-TE\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = db.lookupTable(s, "DEMO_USER").orElse(null);
        assertNotNull(t);

        TableExtractor extractor = new TableExtractor("My", "com.demo.pkg", null, null, null, null);
	    TableModel model = extractor.extractTableModel(createDataTypeLookup(db), t);
        assertNotNull(model);
        assertEquals("MyDemoUser", model.getClassName());
        assertEquals("DemoUser", model.getName());
        assertEquals("com.demo.pkg", model.getPackageName());
        assertEquals("DEMO_USER", model.getSqlName());
        assertEquals(1, model.getForeignKeys().size());
        assertEquals(7, model.getColumns().size()); // Due to #12 the FK-column gets counted as well
        assertEquals(6, model.getNonForeignKeyColumns().size()); // Test for #12 without FK-column
        assertTrue(model.hasColumnsAndForeignKeys());
    }

    private DataTypeLookup createDataTypeLookup(Catalog db) {
	    return new AllDataTypesExtractor(new DataTypeExtractor()).extractIntoDataTypeLookup(db);
    }

    @Test
    void testExtractTableCompositeForeignKey() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-te", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-exchange-rate.sql");
        assertNotNull(tempFile);
        DatabaseManager.executeScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        Schema s = db.lookupSchema("\"RT-TE\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table exchangeRateTable = db.lookupTable(s, "EXCHANGE_RATE").orElse(null);
        assertNotNull(exchangeRateTable);
        Table exchangeRefTable = db.lookupTable(s, "EXCHANGE_REF").orElse(null);
        assertNotNull(exchangeRateTable);

        TableExtractor extractor = new TableExtractor("My", "com.demo.pkg", null, null, null, null);
        TableModel exchangeRateTableModel = extractor.extractTableModel(createDataTypeLookup(db), exchangeRateTable);
        TableModel exchangeRefTableModel = extractor.extractTableModel(createDataTypeLookup(db), exchangeRefTable);

        assertEquals(1, exchangeRefTableModel.getPrimaryKeyColumns().size());
        assertEquals("ID", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getDbName());
        assertEquals("NUMERIC", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getSqlTypeName());
        assertEquals("java.math.BigDecimal", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getJavaTypeName());
        assertTrue(exchangeRefTableModel.getForeignKeyColumns().isEmpty());

        assertEquals(1, exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().size());
        assertEquals("NAME", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getDbName());
        assertEquals("CHARACTER VARYING", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getSqlTypeName());
        assertEquals("java.lang.String", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getJavaTypeName());

        assertEquals(1, exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().size());
        assertEquals("FIRST_NAME", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getDbName());
        assertEquals("CHARACTER VARYING", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getSqlTypeName());
        assertEquals("java.lang.String", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getJavaTypeName());

        assertEquals(2, exchangeRateTableModel.getForeignKeyColumns().size());
        assertEquals(1, exchangeRateTableModel.getIncomingForeignKeys().size());
        assertEquals("composite", exchangeRateTableModel.getIncomingForeignKeys().get(0).getReferencingAttributeName());

        assertEquals("composite", exchangeRateTableModel.getIncomingForeignKeys().get(0).getReferencingAttributeName());

        ForeignKeyModel compositeForeignKeyModel = exchangeRateTableModel.getForeignKeys().stream()
                .filter(fk -> fk.getJavaPropertyName().equals("composite"))
                .findFirst().orElse(null);

        assertNotNull(compositeForeignKeyModel);

        assertEquals(compositeForeignKeyModel.getReferences().size(), 2);

        Assertions
                .assertThat(compositeForeignKeyModel.getReferences().keySet())
                .containsExactlyInAnyOrder("REFERENCE_ID", "PREV_FIRST_NAME");

        org.junit.jupiter.api.Assertions.assertFalse(compositeForeignKeyModel.isNotNull());
    }

    @Test
    void testConstructorWrongClassPrefix() throws Exception {
        assertThatThrownBy(() -> new TableExtractor("123", "com.test", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Class prefix is invalid");
    }

    @Test
    void testConstructorWrongPackage() throws Exception {
        assertThatThrownBy(() -> new TableExtractor("", "com.123.test", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Package name is invalid");
    }

    @Test
    void testConstructorDefaultPackage() throws Exception {
        assertThatThrownBy(() -> new TableExtractor("", "", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("default package may not be used");
    }

    @Test
    void testExtractWithExcludedReferencedTable() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-te-f", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executeScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new RegularExpressionInclusionRule(".*USER.*"));
        assertNotNull(db);
        Schema s = db.lookupSchema("\"RT-TE-F\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = db.lookupTable(s, "DEMO_USER").orElse(null);
        assertNotNull(t);

        assertThatThrownBy(() -> new MetadataExtractor().extract(db))
                .isInstanceOf(RedGGenerationException.class)
                .hasMessageContaining("foreign key is in an excluded table");
    }
}
