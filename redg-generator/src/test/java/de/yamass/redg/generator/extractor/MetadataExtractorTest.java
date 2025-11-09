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
import de.yamass.redg.models.TableModel;
import de.yamass.redg.util.ScriptRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static de.yamass.redg.generator.CodeGeneratorTest.NO_INFORMATION_SCHEMA_INCLUSION_RULE;

class MetadataExtractorTest {

    @Test
    void testJoinTableProcessing() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:rt-me", "", "");
        Assertions.assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-join-table.sql");
        Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, NO_INFORMATION_SCHEMA_INCLUSION_RULE, new IncludeAll());
        Assertions.assertNotNull(db);

        List<TableModel> models = new MetadataExtractor().extract(db);
        Assertions.assertEquals(3, models.size());
        for (TableModel model : models) {
            if (model.getName().equals("DemoCompany")) {
                Assertions.assertEquals(1, model.getJoinTableSimplifierData().size());
                Assertions.assertTrue(model.getJoinTableSimplifierData().containsKey("GUserWorksAtCompanies"));
                Assertions.assertEquals(Arrays.asList("userIdDemoUser", "this"), model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getConstructorParams());
                Assertions.assertEquals(1, model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getMethodParams().size());
            } else if (model.getName().equals("DemoUser")) {
                Assertions.assertEquals(1, model.getJoinTableSimplifierData().size());
                Assertions.assertTrue(model.getJoinTableSimplifierData().containsKey("GUserWorksAtCompanies"));
                Assertions.assertEquals(Arrays.asList("this", "companyIdDemoCompany"), model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getConstructorParams());
                Assertions.assertEquals(1, model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getMethodParams().size());
            } else {
                Assertions.assertEquals(0, model.getJoinTableSimplifierData().size());
            }
        }
    }
}
