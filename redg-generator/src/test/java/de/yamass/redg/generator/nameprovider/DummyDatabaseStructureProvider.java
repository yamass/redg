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

package de.yamass.redg.generator.nameprovider;

import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DataType;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.Table;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DummyDatabaseStructureProvider {

    static Table getDummyTable(final String name) {
        return new de.yamass.redg.schema.model.MutableTable(null, name);
    }

    static Column getDummyColumn(final String name, final String parentName) {
        Table table = getDummyTable(parentName);
        DataType dataType = new DefaultDataType("VARCHAR", JDBCType.VARCHAR, JDBCType.VARCHAR.getVendorTypeNumber(), null, false, 0);
        return new Column(name, dataType, true, false, table);
    }

    static Column getReferencingDummyColumn(final String name, final String referencingTableName,
            final String referencedParentName) {
        // For tests, we just create a simple column
        return getDummyColumn(name, referencingTableName);
    }

    static ForeignKey getSimpleForeignKey(final String columnName, String sourceTableName, final String targetTableName) {
        Table sourceTable = getDummyTable(sourceTableName);
        Table targetTable = getDummyTable(targetTableName);
        Column sourceColumn = getDummyColumn(columnName, sourceTableName);
        Column targetColumn = getDummyColumn("ID", targetTableName);
        ForeignKeyColumn fkColumn = new ForeignKeyColumn(sourceColumn, targetColumn);
        return new ForeignKey("FK_" + columnName, sourceTable, targetTable, Collections.singletonList(fkColumn));
    }

    static ForeignKey getMultiPartForeignKey(final String fkName, final String targetTableName) {
        Table sourceTable = getDummyTable("SOURCE_TABLE");
        Table targetTable = getDummyTable(targetTableName);
        List<ForeignKeyColumn> fkColumns = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Column sourceCol = getDummyColumn("COL" + i, "SOURCE_TABLE");
            Column targetCol = getDummyColumn("ID" + i, targetTableName);
            fkColumns.add(new ForeignKeyColumn(sourceCol, targetCol));
        }
        return new ForeignKey(fkName, sourceTable, targetTable, fkColumns);
    }
}
