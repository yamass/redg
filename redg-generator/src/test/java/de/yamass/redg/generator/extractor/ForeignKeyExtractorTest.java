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
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.testutil.DatabaseTestUtil;
import de.yamass.redg.models.ForeignKeyColumnModel;
import de.yamass.redg.models.ForeignKeyModel;
import org.junit.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;

import static org.junit.Assert.*;


public class ForeignKeyExtractorTest {

    @Test
    public void testForeignKeyExtraction() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:rt-fe", "", "");
        assertNotNull(dataSource);
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        assertNotNull(tempFile);
        DatabaseManager.executePreparationScripts(dataSource, new File[]{tempFile});
        Catalog db = DatabaseManager.crawlDatabase(dataSource, new IncludeAll(), new IncludeAll());
        assertNotNull(db);

        Schema s = db.lookupSchema("\"RT-FE\".PUBLIC").orElse(null);
        assertNotNull(s);
        Table t = db.lookupTable(s, "DEMO_USER").orElse(null);
        assertNotNull(t);
        assertEquals(1, t.getImportedForeignKeys().size());
        ForeignKey fk = (ForeignKey) t.getImportedForeignKeys().toArray()[0];
        assertNotNull(fk);

        ForeignKeyExtractor extractor = new ForeignKeyExtractor(new DefaultDataTypeProvider(), new DefaultNameProvider(),
                new DefaultExplicitAttributeDecider(),"My");
        ForeignKeyModel model = extractor.extractForeignKeyModel(fk);
        assertEquals("MyDemoCompany", model.getJavaTypeName());
        assertEquals("worksAtDemoCompany", model.getName());
        assertEquals(1, model.getReferences().size());
        assertTrue(model.getReferences().containsKey("WORKS_AT"));
        ForeignKeyColumnModel columnModel = model.getReferences().get("WORKS_AT");
        assertEquals("id", columnModel.getPrimaryKeyAttributeName());
        assertEquals("worksAt", columnModel.getLocalName());
        assertEquals("java.math.BigDecimal", columnModel.getLocalType());
        assertEquals("WORKS_AT", columnModel.getDbName());
        assertEquals("DEMO_USER", columnModel.getDbTableName());
    }
}
