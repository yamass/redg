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

import com.btc.redg.generator.Helpers;
import com.btc.redg.models.TableModel;
import org.junit.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.inclusionrule.IncludeAll;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class MetadataExtractorTest {

    @Test
    public void testJoinTableProcessing() throws Exception {
        DatabaseConnectionSource databaseConnectionSource = DatabaseManager.createConnectionSource("jdbc:h2:mem:rt-me", "", "");
        assertNotNull(databaseConnectionSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test-join-table.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(databaseConnectionSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(databaseConnectionSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        List<TableModel> models = MetadataExtractor.extract(db);
        assertEquals(3, models.size());
        for (TableModel model : models) {
            if (model.getName().equals("DemoCompany")) {
                assertEquals(1, model.getJoinTableSimplifierData().size());
                assertTrue(model.getJoinTableSimplifierData().containsKey("GUserWorksAtCompanies"));
                assertEquals(Arrays.asList("this", "userIdDemoUser"), model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getConstructorParams());
                assertEquals(1, model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getMethodParams().size());
            } else if (model.getName().equals("DemoUser")) {
                assertEquals(1, model.getJoinTableSimplifierData().size());
                assertTrue(model.getJoinTableSimplifierData().containsKey("GUserWorksAtCompanies"));
                assertEquals(Arrays.asList("companyIdDemoCompany", "this"), model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getConstructorParams());
                assertEquals(1, model.getJoinTableSimplifierData().get("GUserWorksAtCompanies").getMethodParams().size());
            } else {
                assertEquals(0, model.getJoinTableSimplifierData().size());
            }
        }
    }
}
