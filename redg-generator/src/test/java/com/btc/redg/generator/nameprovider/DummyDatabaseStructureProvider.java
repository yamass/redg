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

package com.btc.redg.generator.nameprovider;

import schemacrawler.schema.*;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DummyDatabaseStructureProvider {

    static Table getDummyTable(final String name) {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn(name);
        return table;
    }

    static Column getDummyColumn(final String name, final String parentName) {
        Table table = getDummyTable(parentName);
        Column column = mock(Column.class);
        when(column.getName()).thenReturn(name);
        when(column.getParent()).thenReturn(table);
        return column;
    }

    static Column getReferencingDummyColumn(final String name, final String referencingTableName,
            final String referencedParentName) {
        Table referencingTable = getDummyTable(referencingTableName);
        Table referencedTable = getDummyTable(referencedParentName);
        Column refColumn = mock(Column.class);
        when(refColumn.getParent()).thenReturn(referencedTable);
        Column column = mock(Column.class);
        when(column.getName()).thenReturn(name);
        when(column.getReferencedColumn()).thenReturn(refColumn);
        when(column.getParent()).thenReturn(referencingTable);
        return column;
    }

    static ForeignKey getSimpleForeignKey(final String columnName, String sourceTableName, final String targetTableName) {
        Column c = getReferencingDummyColumn(columnName, sourceTableName, targetTableName);
        ForeignKey fk = mock(ForeignKey.class);
        ColumnReference reference = mock(ColumnReference.class);
        when(reference.getForeignKeyColumn()).thenReturn(c);
        when(fk.getColumnReferences()).thenReturn(Collections.singletonList(reference));
        return fk;
    }

    static ForeignKey getMultiPartForeignKey(final String fkName, final String targetTableName) {
        Column c = getDummyColumn("", targetTableName);
        ForeignKey fk = mock(ForeignKey.class);
        ColumnReference reference = mock(ColumnReference.class);
        when(reference.getForeignKeyColumn()).thenReturn(c);
        when(fk.getColumnReferences()).thenReturn(Arrays.asList(reference, reference, reference));
        when(fk.getName()).thenReturn(fkName);
        return fk;
    }
}
