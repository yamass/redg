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
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.Table;

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
        if (mappings.containsKey(table.name())) {
            return mappings.get(table.name()).getName();
        }
        return null;
    }

    @Override
    public String getMethodNameForColumn(final Column column, final Table table) {
        if (mappings.containsKey(table.name())) {
            if (mappings.get(table.name()).getColumns() != null &&
                    mappings.get(table.name()).getColumns().containsKey(column.name())) {
                return mappings.get(table.name()).getColumns().get(column.name());
            }
        }
        return null;
    }

    @Override
    public String getMethodNameForForeignKeyColumn(ForeignKeyColumn foreignKeyColumn, Table sourceTable) {
        return getMethodNameForColumn(foreignKeyColumn.sourceColumn(), sourceTable);
    }

    @Override
    public String getMethodNameForReference(final ForeignKey foreignKey) {
        final String tableName = foreignKey.sourceTable().name();
        if (mappings.containsKey(tableName)) {
            // Note: ForeignKey doesn't have a name in our model, so we can't look it up by name
            // This might need to be adjusted based on how the JSON mapping is structured
            // For now, return null as the mapping structure may not support this
        }
        return null;
    }

    @Override
    public String getMethodNameForIncomingForeignKey(ForeignKey foreignKey) {
        return null; // TODO
    }
}
