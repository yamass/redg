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

import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.models.ConvenienceSetterModel;
import de.yamass.redg.models.TableModel;
import de.yamass.redg.util.ScriptRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class CodeGeneratorTest {

    @Test
    void testGenerateCodeForTable() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-tt", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, null),
                "G",
                "de.yamass.redg.generated",
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new ExplicitAttributeDecider() {
                    @Override
                    public boolean isExplicitAttribute(final de.yamass.redg.schema.model.Column column, final de.yamass.redg.schema.model.Table table) {
                        return column.name().equals("DTYPE");
                    }

                    @Override
                    public boolean isExplicitForeignKey(final de.yamass.redg.schema.model.ForeignKey foreignKey) {
                        return false;
                    }
                },
                ConvenienceSetterProvider.NONE);
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(model);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(model, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/tableResult.java", result);

        String existingClassResult = cg.generateExistingClassCodeForTable(model);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/tableResultExisting.java", existingClassResult);
    }

    @Test
    void testGenerateCodeWithMultipartForeignKey() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-mpfk", "", "");
        Assertions.assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-multipart-fk.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, null),
                Constants.DEFAULT_CLASS_PREFIX,
                Constants.DEFAULT_TARGET_PACKAGE,
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider(),
                ConvenienceSetterProvider.NONE);
        TableModel demoUser = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        TableModel demoCompany = models.stream().filter(m -> Objects.equals("DemoCompany", m.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(demoUser);
        Assertions.assertNotNull(demoCompany);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(demoUser, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/table-multipart-Result.java", result);

        String existingClassResult = cg.generateExistingClassCodeForTable(demoUser);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/table-multipart-Result-Existing.java", existingClassResult);

        result = cg.generateCodeForTable(demoCompany, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/table-multipart-Result2.java", result);

        existingClassResult = cg.generateExistingClassCodeForTable(demoCompany);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/table-multipart-Result2-Existing.java", existingClassResult);
    }

    @Test
    void testGenerateMainClass() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-main", "", "");
        Assertions.assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, List.of("PUBLIC")),
                Constants.DEFAULT_CLASS_PREFIX,
                Constants.DEFAULT_TARGET_PACKAGE,
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider(),
                ConvenienceSetterProvider.NONE);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateMainClass(models, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/mainResult.java", result);
    }

    @Test
    void testGenerateCodeJoinHelper() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-jt", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-join-table.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, null),
                Constants.DEFAULT_CLASS_PREFIX,
                Constants.DEFAULT_TARGET_PACKAGE,
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider(),
                ConvenienceSetterProvider.NONE);
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(model);

        CodeGenerator generator = new CodeGenerator();
        String result = generator.generateCodeForTable(model, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/table-join-Result.java", result);

        String existingClassResult = generator.generateExistingClassCodeForTable(model);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/table-join-Result-Existing.java", existingClassResult);
    }

    @Test
    void testGenerateConvenienceMethods() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-dcm", "", "");
        Assertions.assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-date-convenience.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, null),
                Constants.DEFAULT_CLASS_PREFIX,
                Constants.DEFAULT_TARGET_PACKAGE,
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider(),
                (column, table, javaDataTypeName) -> {
                    if (javaDataTypeName.equals("java.sql.Timestamp")) {
                        return Collections.singletonList(new ConvenienceSetterModel("java.util.String", "de.yamass.redg.runtime.util.DateConverter.convertDate"));
                    } else {
                        return Collections.emptyList();
                    }
                });
        TableModel model = models.stream().filter(m -> Objects.equals("DatesTable", m.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(model);

        CodeGenerator generator = new CodeGenerator();
        String result = generator.generateCodeForTable(model, false);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/table-date-convenience-Result.java", result);

        String existingClassResult = generator.generateExistingClassCodeForTable(model);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/table-date-convenience-Result-Existing.java", existingClassResult);
    }

    @Test
    void testEnableVisualization() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testEnableVisualization", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, null),
                "G",
                "de.yamass.redg.generated",
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new ExplicitAttributeDecider() {
                    @Override
                    public boolean isExplicitAttribute(final de.yamass.redg.schema.model.Column column, final de.yamass.redg.schema.model.Table table) {
                        return column.name().equals("DTYPE");
                    }

                    @Override
                    public boolean isExplicitForeignKey(final de.yamass.redg.schema.model.ForeignKey foreignKey) {
                        return false;
                    }
                },
                ConvenienceSetterProvider.NONE);
        TableModel model = models.stream().filter(m -> Objects.equals("DemoUser", m.getName())).findFirst().orElse(null);
        Assertions.assertNotNull(model);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateCodeForTable(model, true);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/tableResult-wV.java", result);

        String existingClassResult = cg.generateExistingClassCodeForTable(model);
        Assertions.assertNotNull(existingClassResult);
        compareResultWithExpected("codegenerator/tableResultExisting-wV.java", existingClassResult);
    }

    @Test
    void testGenerateMainClassWithVisualization() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-cg-main-viz", "", "");
        Assertions.assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        List<TableModel> models = RedGGenerator.transformSchemaModel(
                RedGGenerator.inspectSchemas(dataSource, List.of("PUBLIC")),
                Constants.DEFAULT_CLASS_PREFIX,
                Constants.DEFAULT_TARGET_PACKAGE,
                new DefaultDataTypeProvider(),
                new DefaultNameProvider(),
                new de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider(),
                ConvenienceSetterProvider.NONE);

        CodeGenerator cg = new CodeGenerator();
        String result = cg.generateMainClass(models, true);
        Assertions.assertNotNull(result);
        compareResultWithExpected("codegenerator/mainResult-wV.java", result);
    }

    private void compareResultWithExpected(String resourcePath, String actual) {
//	    try {
//		    Path path = Path.of("./src/test/resources").resolve(resourcePath);
//		    System.out.println(path.toAbsolutePath().toString());
//		    Files.writeString(path, actual);
//	    } catch (IOException e) {
//		    e.printStackTrace();
//	    }
        String expected = Helpers.getResourceAsString(resourcePath);
        expected = expected.replaceAll("private static String serializedTableModel = .*", "");
        actual = actual.replaceAll("private static String serializedTableModel = .*", "");
        Assertions.assertEquals(expected, actual);
    }
}
