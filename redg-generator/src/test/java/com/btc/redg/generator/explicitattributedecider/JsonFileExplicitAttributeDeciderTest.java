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

package com.btc.redg.generator.explicitattributedecider;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.btc.redg.generator.extractor.explicitattributedecider.JsonFileExplicitAttributeDecider;

import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonFileExplicitAttributeDeciderTest {
    @Test
    public void isExplicitAttribute() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest.json")));

        Table tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.getName()).thenReturn("TABLENAME");
        Column columnMock = Mockito.mock(Column.class);
        Mockito.when(columnMock.getName()).thenReturn("EXPLICIT");
        Mockito.when(columnMock.getParent()).thenReturn(tableMock);
        Assert.assertTrue(decider.isExplicitAttribute(columnMock));

        Mockito.when(columnMock.getName()).thenReturn("NONEXPLICIT");
        Assert.assertFalse(decider.isExplicitAttribute(columnMock));
    }

    @Test
    public void isExplicitAttributeDefinitionMissing() throws Exception {
        JsonFileExplicitAttributeDecider decider =
                new JsonFileExplicitAttributeDecider(new InputStreamReader(getClass().getResourceAsStream("JsonFileExplicitAttributeDeciderTest2.json")));
        Table tableMock = Mockito.mock(Table.class);
        Mockito.when(tableMock.getName()).thenReturn("TABLENAME");
        Column columnMock = Mockito.mock(Column.class);
        Mockito.when(columnMock.getName()).thenReturn("EXPLICIT");
        Mockito.when(columnMock.getParent()).thenReturn(tableMock);
        Assert.assertFalse(decider.isExplicitAttribute(columnMock));

        Mockito.when(columnMock.getName()).thenReturn("NONEXPLICIT");
        Assert.assertFalse(decider.isExplicitAttribute(columnMock));
    }

    @Test
    public void isExplicitForeignKey() throws Exception {
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

        assertFalse(decider.isExplicitForeignKey(fk1Mock));

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

        assertTrue(decider.isExplicitForeignKey(fk2Mock));
    }

}