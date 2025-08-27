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

package de.yamass.redg.generator;

import de.yamass.redg.generator.extractor.DatabaseManager;
import de.yamass.redg.generator.extractor.MetadataExtractor;
import de.yamass.redg.generator.extractor.TableExtractor;
import de.yamass.redg.generator.extractor.conveniencesetterprovider.DefaultConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.testutil.DatabaseTestUtil;
import de.yamass.redg.models.ConvenienceSetterModel;
import de.yamass.redg.models.TableModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class CodeGeneratorTest {

    public static final InclusionRule NO_INFORMATION_SCHEMA_INCLUSION_RULE = s -> !s.toLowerCase().replace("\"", "").contains(".information_schema");
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGenerateCodeForTable() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-tt", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream().filter(schema -> schema.getName().equals("PUBLIC")).findFirst().orElse(null);
        assertNotNull(s);
        Table t = db.getTables(s).stream().filter(table -> table.getName().equals("DEMO_USER")).findFirst().orElse(null);
        assertNotNull(t);

        List<TableModel> models = MetadataExtractor.extract(db, new TableExtractor("G", "de.yamass.redg.generated",
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
    public void testGenerateCodeWithMultipartForeignKey() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-cg-mpfk", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-multipart-fk.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
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
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
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
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
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
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
        assertNotNull(db);

        TableExtractor tableExtractor = new TableExtractor(
                TableExtractor.DEFAULT_CLASS_PREFIX,
                TableExtractor.DEFAULT_TARGET_PACKAGE,
                null, null, null, (column, javaDataTypeName) -> {
                    if (javaDataTypeName.equals("java.sql.Timestamp")) {
                        return Collections.singletonList(new ConvenienceSetterModel("java.util.String", "de.yamass.redg.runtime.util.DateConverter.convertDate"));
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
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
        assertNotNull(db);
        Schema s = db.getSchemas().stream().filter(schema -> schema.getName().equals("PUBLIC")).findFirst().orElse(null);
        assertNotNull(s);
        Table t = db.getTables(s).stream().filter(table -> table.getName().equals("DEMO_USER")).findFirst().orElse(null);
        assertNotNull(t);

        List<TableModel> models = MetadataExtractor.extract(db, new TableExtractor("G", "de.yamass.redg.generated",
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
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
        assertNotNull(db);

        List<TableModel> models = MetadataExtractor.extract(db);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateMainClass(models, true);
        assertNotNull(result);
        assertEquals(Helpers.getResourceAsString("codegenerator/mainResult-wV.java"), result);
    }
}
