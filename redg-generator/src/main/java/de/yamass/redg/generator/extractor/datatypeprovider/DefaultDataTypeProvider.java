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

package de.yamass.redg.generator.extractor.datatypeprovider;

import org.jspecify.annotations.NonNull;
import schemacrawler.schema.Column;

/**
 * The default data type provider, used if nothing else is specified
 */
public class DefaultDataTypeProvider implements DataTypeProvider {

    /**
     * Simply returns the data type advised by SchemaCrawler.
     *
     * This needs to be based on columns because the column definition adds parameters to the type! E.g. VARCHAR(10), NUMBER(22, 2).
     *
     * @param column The current column
     * @return The data type advised by SchemaCrawler
     */
    @Override
    public @NonNull String getCanonicalDataTypeName(Column column) {
        return column.getColumnDataType().getTypeMappedClass().getCanonicalName();
    }
}
