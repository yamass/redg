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

import de.yamass.redg.generator.utils.TypeMap;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.Table;
import org.jspecify.annotations.NonNull;

/**
 * The default data type provider, used if nothing else is specified.
 */
public class DefaultDataTypeProvider implements DataTypeProvider {

    /**
     * Returns the Java class name based on the JDBC type mapping.
     * For arrays, this returns the base type's mapped class (not the array class).
     *
     * This needs to be based on columns because the column definition adds parameters to the type! E.g. VARCHAR(10), NUMBER(22, 2).
     *
     * @param column The current column
     * @param table The table containing the column
     * @return The canonical name of the Java class that corresponds to the column's JDBC type
     */
    @Override
    public @NonNull String getCanonicalDataTypeName(Column column, Table table) {
        de.yamass.redg.schema.model.DataType dataType = column.type();
        
        if (dataType.isArray() && dataType.getBaseType() != null) {
            return TypeMap.getCanonicalName(dataType.getBaseType().getJdbcType());
        }
        
        return TypeMap.getCanonicalName(dataType.getJdbcType());
    }
}
