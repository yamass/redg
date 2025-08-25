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

package de.yamass.redg.generator.extractor.nameprovider.json;

import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class JsonFileNameProvider implements NameProvider {

    private final HashMap<String, JsonTableNameData> mappings;

    public JsonFileNameProvider(final File jsonFile) throws IOException {
        Objects.requireNonNull(jsonFile);
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, JsonTableNameData>> typeReference = new TypeReference<HashMap<String, JsonTableNameData>>() {
        };
        mappings = mapper.readValue(jsonFile, typeReference);
    }

    @Override
    public String getClassNameForTable(final Table table) {
        if (mappings.containsKey(table.getName())) {
            return mappings.get(table.getName()).getName();
        }
        return null;
    }

    @Override
    public String getMethodNameForColumn(final Column column) {
        if (mappings.containsKey(column.getParent().getName())) {
            if (mappings.get(column.getParent().getName()).getColumns() != null &&
                    mappings.get(column.getParent().getName()).getColumns().containsKey(column.getName())) {
                return mappings.get(column.getParent().getName()).getColumns().get(column.getName());
            }
        }
        return null;
    }

    @Override
    public String getMethodNameForForeignKeyColumn(ForeignKey foreignKey, Column primaryKeyColumn, Column foreignKeyColumn) {
        return getMethodNameForColumn(foreignKeyColumn);
    }

    @Override
    public String getMethodNameForReference(final ForeignKey foreignKey) {
        ColumnReference firstForeignKeyColumnReferences = foreignKey.getColumnReferences().get(0);
        final String tableName = firstForeignKeyColumnReferences.getForeignKeyColumn().getParent().getName();
        if (mappings.containsKey(tableName)) {
            if (mappings.get(tableName).getColumns() != null &&
                    mappings.get(tableName).getColumns().containsKey(foreignKey.getName())) {
                return mappings.get(tableName).getColumns().get(foreignKey.getName());
            }
        }
        return null;
    }

    @Override
    public String getMethodNameForIncomingForeignKey(ForeignKey foreignKey) {
        return null; // TODO
    }
}
