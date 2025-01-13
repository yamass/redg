/*
 * Copyright 2017 BTC Business Technology AG
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

package com.btc.redg.generator.extractor;

import java.io.File;
import java.sql.Connection;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.btc.redg.generator.Helpers;
import com.btc.redg.generator.exceptions.RedGGenerationException;
import com.btc.redg.models.ForeignKeyModel;
import com.btc.redg.models.TableModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;


public class TableExtractorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testExtractTable() throws Exception {
        Connection connection = DatabaseManager.connectToDatabase("org.h2.Driver", "jdbc:h2:mem:rt-te", "", "");
        assertNotNull(connection);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(connection, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(connection, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        Schema s = db.lookupSchema("\"RT-TE\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = db.lookupTable(s, "DEMO_USER").orElse(null);
        assertNotNull(t);

        TableExtractor extractor = new TableExtractor("My", "com.demo.pkg", null, null, null, null);
        TableModel model = extractor.extractTableModel(t);
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

    @Test
    public void testExtractTableCompositeForeignKey() throws Exception {
        Connection connection = DatabaseManager.connectToDatabase("org.h2.Driver", "jdbc:h2:mem:rt-te", "", "");
        assertNotNull(connection);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-exchange-rate.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(connection, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(connection, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        Schema s = db.lookupSchema("\"RT-TE\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table exchangeRateTable = db.lookupTable(s, "EXCHANGE_RATE").orElse(null);
        assertNotNull(exchangeRateTable);
        Table exchangeRefTable = db.lookupTable(s, "EXCHANGE_REF").orElse(null);
        assertNotNull(exchangeRateTable);

        TableExtractor extractor = new TableExtractor("My", "com.demo.pkg", null, null, null, null);
        TableModel exchangeRateTableModel = extractor.extractTableModel(exchangeRateTable);
        TableModel exchangeRefTableModel = extractor.extractTableModel(exchangeRefTable);

        Assert.assertEquals(1, exchangeRefTableModel.getPrimaryKeyColumns().size());
        Assert.assertEquals("ID", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getDbName());
        Assert.assertEquals("DECIMAL", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getSqlType());
        Assert.assertEquals("java.math.BigDecimal", exchangeRefTableModel.getPrimaryKeyColumns().get(0).getJavaTypeName());
        Assert.assertTrue(exchangeRefTableModel.getForeignKeyColumns().isEmpty());

        Assert.assertEquals(1, exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().size());
        Assert.assertEquals("NAME", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getDbName());
        Assert.assertEquals("VARCHAR", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getSqlType());
        Assert.assertEquals("java.lang.String", exchangeRefTableModel.getNonPrimaryKeyNonFKColumns().get(0).getJavaTypeName());

        Assert.assertEquals(1, exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().size());
        Assert.assertEquals("FIRST_NAME", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getDbName());
        Assert.assertEquals("VARCHAR", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getSqlType());
        Assert.assertEquals("java.lang.String", exchangeRateTableModel.getNonPrimaryKeyNonFKColumns().get(0).getJavaTypeName());

        Assert.assertEquals(2, exchangeRateTableModel.getForeignKeyColumns().size());
        Assert.assertEquals(1, exchangeRateTableModel.getIncomingForeignKeys().size());
        Assert.assertEquals("composite", exchangeRateTableModel.getIncomingForeignKeys().get(0).getReferencingAttributeName());

        Assert.assertEquals("composite", exchangeRateTableModel.getIncomingForeignKeys().get(0).getReferencingAttributeName());

        ForeignKeyModel compositeForeignKeyModel = exchangeRateTableModel.getForeignKeys().stream()
                .filter(fk -> fk.getName().equals("composite"))
                .findFirst().orElse(null);

        Assert.assertNotNull(compositeForeignKeyModel);

        Assert.assertEquals(compositeForeignKeyModel.getReferences().size(), 2);

        Assertions
                .assertThat(compositeForeignKeyModel.getReferences().keySet())
                .containsExactlyInAnyOrder("REFERENCE_ID", "PREV_FIRST_NAME");

        Assert.assertFalse(compositeForeignKeyModel.isNotNull());
    }

    @Test
    public void testConstructorWrongClassPrefix() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Class prefix is invalid");

        new TableExtractor("123", "com.test", null, null, null, null);
    }

    @Test
    public void testConstructorWrongPackage() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Package name is invalid");

        new TableExtractor("", "com.123.test", null, null, null, null);
    }

    @Test
    public void testConstructorDefaultPackage() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("default package may not be used");

        new TableExtractor("", "", null, null, null, null);
    }

    @Test
    public void testExtractWithExcludedReferencedTable() throws Exception {
        thrown.expect(RedGGenerationException.class);
        thrown.expectMessage("foreign key is in an excluded table");

        Connection connection = DatabaseManager.connectToDatabase("org.h2.Driver", "jdbc:h2:mem:rt-te-f", "", "");
        assertNotNull(connection);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(connection, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(connection, new IncludeAll(), new RegularExpressionInclusionRule(".*USER.*"));
        assertNotNull(db);
        Schema s = db.lookupSchema("\"RT-TE-F\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = db.lookupTable(s, "DEMO_USER").orElse(null);
        assertNotNull(t);

        MetadataExtractor.extract(db);
    }
}
