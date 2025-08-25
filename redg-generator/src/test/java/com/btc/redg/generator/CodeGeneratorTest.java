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

package com.btc.redg.generator;

import com.btc.redg.generator.extractor.DatabaseManager;
import com.btc.redg.generator.extractor.MetadataExtractor;
import com.btc.redg.generator.extractor.TableExtractor;
import com.btc.redg.generator.extractor.conveniencesetterprovider.DefaultConvenienceSetterProvider;
import com.btc.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import com.btc.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import com.btc.redg.generator.extractor.nameprovider.DefaultNameProvider;
import com.btc.redg.generator.testutil.DatabaseTestUtil;
import com.btc.redg.models.ConvenienceSetterModel;
import com.btc.redg.models.TableModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


public class CodeGeneratorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGenerateCodeForTable() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-tt", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream().filter(schema -> schema.getName().equals("PUBLIC")).findFirst().orElse(null);
        assertNotNull(s);
        Table t = db.getTables(s).stream().filter(table -> table.getName().equals("DEMO_USER")).findFirst().orElse(null);
        assertNotNull(t);

        List<TableModel> models = MetadataExtractor.extract(db, new TableExtractor("G", "com.btc.redg.generated",
                new DefaultDataTypeProvider(), new DefaultNameProvider(), new ExplicitAttributeDecider() {
            @Override
            public boolean isExplicitAttribute(final Column column) {
                return column.getName().equals("DTYPE");
            }

            @Override
            public boolean isExplicitForeignKey(final ForeignKey foreignKey) {
                return false;
            }
        },
                new DefaultConvenienceSetterProvider()));
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        assertNotNull(model);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(model, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/tableResult.java"), result);

        String existingClassResult = cg.generateExistingClassCodeForTable(model);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/tableResultExisting.java"), existingClassResult);
    }

    @Test
    public void testGenerateCodeEscaping() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-te", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-escaping.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream().filter(schema -> schema.getName().equals("PUBLIC")).findFirst().orElse(null);
        assertNotNull(s);
        Table t1 = db.getTables(s).stream().filter(table -> table.getName().equals("TABLE")).findFirst().orElse(null);
        assertNotNull(t1);
        Table t2 = db.getTables(s).stream().filter(table -> table.getName().equals("GROUP")).findFirst().orElse(null);
        assertNotNull(t2);

        List<TableModel> models = MetadataExtractor.extract(db);
        assertNotNull(models);
        assertThat(models.size(), is(2));

        TableModel modelTable = models.stream().filter(m -> Objects.equals("Table", m.getName())).findFirst().orElse(null);
        assertNotNull(modelTable);
        TableModel modelGroup = models.stream().filter(m -> Objects.equals("Group", m.getName())).findFirst().orElse(null);
        assertNotNull(modelGroup);
        CodeGenerator cg = new CodeGenerator();

        String resultTable = cg.generateCodeForTable(modelTable, false);
        String resultGroup = cg.generateCodeForTable(modelGroup, false);

        assertNotNull(resultTable);
        assertNotNull(resultGroup);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-escaping-Result-table.java"), resultTable);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-escaping-Result-group.java"), resultGroup);

        //TODO: add test for existing class
    }

    @Test
    public void testGenerateCodeWithMultipartForeignKey() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-mpfk", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-multipart-fk.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream()
                .filter(schema -> schema.getName().equals("PUBLIC"))
                .findFirst().orElse(null);
        assertNotNull(s);
        Table t = db.getTables(s).stream()
                .filter(table -> table.getName().equals("DEMO_USER"))
                .findFirst().orElse(null);
        assertNotNull(t);

        List<TableModel> models = MetadataExtractor.extract(db);
        TableModel demoUser = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        TableModel demoCompany = models.stream().filter(m -> Objects.equals("DemoCompany", m.getName())).findFirst().orElse(null);
        assertNotNull(demoUser);
        assertNotNull(demoCompany);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(demoUser, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-multipart-Result.java"), result);

        String existingClassResult = cg.generateExistingClassCodeForTable(demoUser);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-multipart-Result-Existing.java"), existingClassResult);

        result = cg.generateCodeForTable(demoCompany, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-multipart-Result2.java"), result);

        existingClassResult = cg.generateExistingClassCodeForTable(demoCompany);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-multipart-Result2-Existing.java"), existingClassResult);
    }

    @Test
    public void testGenerateMainClass() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-main", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        List<TableModel> models = MetadataExtractor.extract(db);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateMainClass(models, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/mainResult.java"), result);
    }

    @Test
    public void testGenerateCodeJoinHelper() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-jt", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-join-table.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        List<TableModel> models = MetadataExtractor.extract(db);
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        assertNotNull(model);

        CodeGenerator generator = new CodeGenerator();
        String result = generator.generateCodeForTable(model, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-join-Result.java"), result);

        String existingClassResult = generator.generateExistingClassCodeForTable(model);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-join-Result-Existing.java"), existingClassResult);
    }

    @Test
    public void testGenerateConvenienceMethods() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-dcm", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-date-convenience.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        TableExtractor tableExtractor = new TableExtractor(
                TableExtractor.DEFAULT_CLASS_PREFIX,
                TableExtractor.DEFAULT_TARGET_PACKAGE,
                null, null, null, (column, javaDataTypeName) -> {
                    if (javaDataTypeName.equals("java.sql.Timestamp")) {
                        return Collections.singletonList(new ConvenienceSetterModel("java.util.String", "com.btc.redg.runtime.util.DateConverter.convertDate"));
                    } else {
                        return Collections.emptyList();
                    }
                }
        );
        List<TableModel> models = MetadataExtractor.extract(db, tableExtractor);
        TableModel model = models.stream().filter(m -> Objects.equals("DatesTable", m.getName())).findFirst().orElse(null);
        assertNotNull(model);

        CodeGenerator generator = new CodeGenerator();
        String result = generator.generateCodeForTable(model, false);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-date-convenience-Result.java"), result);

        String existingClassResult = generator.generateExistingClassCodeForTable(model);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/table-date-convenience-Result-Existing.java"), existingClassResult);
    }

    @Test
    public void testEnableVisualization() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:testEnableVisualization", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream().filter(schema -> schema.getName().equals("PUBLIC")).findFirst().orElse(null);
        assertNotNull(s);
        Table t = db.getTables(s).stream().filter(table -> table.getName().equals("DEMO_USER")).findFirst().orElse(null);
        assertNotNull(t);

        List<TableModel> models = MetadataExtractor.extract(db, new TableExtractor("G", "com.btc.redg.generated",
                new DefaultDataTypeProvider(), new DefaultNameProvider(), new ExplicitAttributeDecider() {
            @Override
            public boolean isExplicitAttribute(final Column column) {
                return column.getName().equals("DTYPE");
            }

            @Override
            public boolean isExplicitForeignKey(final ForeignKey foreignKey) {
                return false;
            }
        },
                new DefaultConvenienceSetterProvider()));
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        assertNotNull(model);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(model, true);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/tableResult-wV.java"), result);

        String existingClassResult = cg.generateExistingClassCodeForTable(model);
        assertNotNull(existingClassResult);
        assertEquals(Helpers.getResourceAsString("codegenerator/tableResultExisting-wV.java"), existingClassResult);
    }

    @Test
    public void testGenerateMainClassWithVisualization() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-main-viz", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        List<TableModel> models = MetadataExtractor.extract(db);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateMainClass(models, true);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/mainResult-wV.java"), result);
    }
}
