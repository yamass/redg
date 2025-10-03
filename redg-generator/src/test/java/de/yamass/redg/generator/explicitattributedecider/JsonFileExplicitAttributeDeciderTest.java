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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

class JsonFileExplicitAttributeDeciderTest {
    @Test
    void isExplicitAttribute() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest.json")));

        Table tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.getName()).thenReturn("TABLENAME");
        Column columnMock = Mockito.mock(Column.class);
        Mockito.when(columnMock.getName()).thenReturn("EXPLICIT");
        Mockito.when(columnMock.getParent()).thenReturn(tableMock);
        Assertions.assertTrue(decider.isExplicitAttribute(columnMock));

        Mockito.when(columnMock.getName()).thenReturn("NONEXPLICIT");
        Assertions.assertFalse(decider.isExplicitAttribute(columnMock));
    }

    @Test
    void isExplicitAttributeDefinitionMissing() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest2.json")));
        Table tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.getName()).thenReturn("TABLENAME");
        Column columnMock = Mockito.mock(Column.class);
        Mockito.when(columnMock.getName()).thenReturn("EXPLICIT");
        Mockito.when(columnMock.getParent()).thenReturn(tableMock);
        Assertions.assertFalse(decider.isExplicitAttribute(columnMock));

        Mockito.when(columnMock.getName()).thenReturn("NONEXPLICIT");
        Assertions.assertFalse(decider.isExplicitAttribute(columnMock));
    }

    @Test
    void isExplicitForeignKey() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest.json")));
        Table tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.getName()).thenReturn("TABLENAME");

        ForeignKey fk1Mock = Mockito.mock(ForeignKey.class);

        ColumnReference fkcr1Mock = Mockito.mock(ColumnReference.class);

        Column c1Mock = Mockito.mock(Column.class);
        Mockito.when(c1Mock.getName()).thenReturn("NOPE");
        Mockito.when(c1Mock.getParent()).thenReturn(tableMock);

        Mockito.when(fkcr1Mock.getForeignKeyColumn()).thenReturn(c1Mock);

        Mockito.when(fk1Mock.getColumnReferences()).thenReturn(Collections.singletonList(fkcr1Mock));

        Assertions.assertFalse(decider.isExplicitForeignKey(fk1Mock));

        ForeignKey fk2Mock = Mockito.mock(ForeignKey.class);

        ColumnReference fkcr2Mock = Mockito.mock(ColumnReference.class);

        Column c2Mock = Mockito.mock(Column.class);
        Mockito.when(c2Mock.getName()).thenReturn("FOREIGNKEY1");
        Mockito.when(c2Mock.getParent()).thenReturn(tableMock);

        Mockito.when(fkcr2Mock.getForeignKeyColumn()).thenReturn(c2Mock);

        ColumnReference fkcr3Mock = Mockito.mock(ColumnReference.class);

        Column c3Mock = Mockito.mock(Column.class);
        Mockito.when(c3Mock.getName()).thenReturn("PART2");
        Mockito.when(c3Mock.getParent()).thenReturn(tableMock);

        Mockito.when(fkcr3Mock.getForeignKeyColumn()).thenReturn(c3Mock);

        Mockito.when(fk2Mock.getColumnReferences()).thenReturn(Arrays.asList(fkcr2Mock, fkcr3Mock));

        Assertions.assertTrue(decider.isExplicitForeignKey(fk2Mock));
    }

}