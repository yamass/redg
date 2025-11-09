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

package de.yamass.redg.generator.explicitattributedecider;

import de.yamass.redg.generator.extractor.explicitattributedecider.JsonFileExplicitAttributeDecider;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.sql.JDBCType;
import java.util.Collections;

class JsonFileExplicitAttributeDeciderTest {
    @Test
    void isExplicitAttribute() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest.json")));

        Table table = new de.yamass.redg.schema.model.MutableTable(null, "TABLENAME");
        DefaultDataType dataType = new DefaultDataType("VARCHAR", JDBCType.VARCHAR, JDBCType.VARCHAR.getVendorTypeNumber(), null, false, 0);
        Column explicitColumn = new Column("EXPLICIT", dataType, true, false, table);
        Column nonExplicitColumn = new Column("NONEXPLICIT", dataType, true, false, table);
        
        Assertions.assertTrue(decider.isExplicitAttribute(explicitColumn, table));
        Assertions.assertFalse(decider.isExplicitAttribute(nonExplicitColumn, table));
    }

    @Test
    void isExplicitAttributeDefinitionMissing() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest2.json")));
        Table table = new de.yamass.redg.schema.model.MutableTable(null, "TABLENAME");
        DefaultDataType dataType = new DefaultDataType("VARCHAR", JDBCType.VARCHAR, JDBCType.VARCHAR.getVendorTypeNumber(), null, false, 0);
        Column explicitColumn = new Column("EXPLICIT", dataType, true, false, table);
        Column nonExplicitColumn = new Column("NONEXPLICIT", dataType, true, false, table);
        
        Assertions.assertFalse(decider.isExplicitAttribute(explicitColumn, table));
        Assertions.assertFalse(decider.isExplicitAttribute(nonExplicitColumn, table));
    }

    @Test
    void isExplicitForeignKey() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest.json")));
        Table sourceTable = new de.yamass.redg.schema.model.MutableTable(null, "TABLENAME");
        Table targetTable = new de.yamass.redg.schema.model.MutableTable(null, "TARGET");

        DefaultDataType dataType = new DefaultDataType("VARCHAR", JDBCType.VARCHAR, JDBCType.VARCHAR.getVendorTypeNumber(), null, false, 0);
        Column c1 = new Column("NOPE", dataType, true, false, sourceTable);
        Column target1 = new Column("ID", dataType, true, false, targetTable);
        ForeignKeyColumn fkCol1 = new ForeignKeyColumn(c1, target1);
        ForeignKey fk1 = new ForeignKey("FK1", sourceTable, targetTable, Collections.singletonList(fkCol1));

        Assertions.assertFalse(decider.isExplicitForeignKey(fk1));

        Column c2 = new Column("FOREIGNKEY1", dataType, true, false, sourceTable);
        Column c3 = new Column("PART2", dataType, true, false, sourceTable);
        Column target2 = new Column("ID1", dataType, true, false, targetTable);
        Column target3 = new Column("ID2", dataType, true, false, targetTable);
        ForeignKeyColumn fkCol2 = new ForeignKeyColumn(c2, target2);
        ForeignKeyColumn fkCol3 = new ForeignKeyColumn(c3, target3);
        ForeignKey fk2 = new ForeignKey("FK2", sourceTable, targetTable, java.util.Arrays.asList(fkCol2, fkCol3));

        Assertions.assertTrue(decider.isExplicitForeignKey(fk2));
    }

}