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

package de.yamass.redg.generator.extractor.datatypeprovider.json;

import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.helpers.DataTypePrecisionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import schemacrawler.schema.Column;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * A data type provider that reads the types from a json file.
 * </p><p>
 * The format for the file is:
 * </p>
 * <p><blockquote><pre>
 * {
 *     "tableMappings": {
 *         "SCHEMA.TABLE": {
 *             "COLUMN" : "full.class.Name",
 *             "OTHER_COLUMN" : "possibly.other.Class",
 *             ...
 *         },
 *         ...
 *     },
 *     "defaultMappings": {
 *         "DECIMAL": "java.lang.YourFavoriteNumberType",
 *         "DECIMAL(1)": "java.lang.Boolean"
 *     }
 * }
 * </pre></blockquote>
 */
public class JsonFileDataTypeProvider implements DataTypeProvider {

    private final TypeMappings typeMappings;

    private final DataTypeProvider fallbackProvider;

    /**
     * Creates a {@link DataTypeProvider} that uses a JSON file as its data source. If no data type can be read from the JSON, the type is queried from the
     * {@code fallbackProvider}.
     *
     * @param jsonFile         The JSON file that specifies the wanted data types. May not be {@code null}. See class documentation for format.
     * @param fallbackProvider The provider to be queried if no type can be found in the JSON file. May not be {@code null}
     * @throws IOException Gets thrown when the JSON file could not be read or parsed
     */
    public JsonFileDataTypeProvider(File jsonFile, DataTypeProvider fallbackProvider) throws IOException {
        this.fallbackProvider = fallbackProvider;
        Objects.requireNonNull(jsonFile);
        ObjectMapper mapper = new ObjectMapper();
        typeMappings = mapper.readValue(jsonFile, TypeMappings.class);
    }

    @Override
    public String getCanonicalDataTypeName(final Column column) {
        if (typeMappings.getTableMappings() != null) {
            final HashMap<String, String> tableMap = typeMappings.getTableMappings().get(column.getParent().getFullName());
            if (tableMap != null) {
                final String className = tableMap.get(column.getName());
                if (className != null) {
                    return className;
                }
            }
        }
        final HashMap<String, String> defaultMappings = typeMappings.getDefaultTypeMappings();
        if (defaultMappings != null) {
            List<String> variants = DataTypePrecisionHelper.getDataTypeWithAllPrecisionVariants(column);
            for (final String variant : variants) {
                final String defaultType = defaultMappings.get(variant);
                if (defaultType != null) {
                    return defaultType;
                }
            }
        }
        return fallbackProvider.getCanonicalDataTypeName(column);
    }
}
