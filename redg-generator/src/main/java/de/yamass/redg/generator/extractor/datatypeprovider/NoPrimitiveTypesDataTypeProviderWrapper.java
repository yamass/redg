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

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.Table;

public class NoPrimitiveTypesDataTypeProviderWrapper implements DataTypeProvider {

    private static final Map<String, Class<?>> OBJECT_TYPE_BY_PRIMITIVE_TYPE = ImmutableMap.<String, Class<?>>builder()
            .put("boolean", java.lang.Boolean.class)
            .put("char", java.lang.Character.class)
            .put("byte", java.lang.Byte.class)
            .put("short", java.lang.Short.class)
            .put("int", java.lang.Integer.class)
            .put("long", java.lang.Long.class)
            .put("float", java.lang.Float.class)
            .put("double", java.lang.Double.class)
            .build();

    private DataTypeProvider originalDataTypeProvider;

    public NoPrimitiveTypesDataTypeProviderWrapper(DataTypeProvider originalDataTypeProvider) {
        this.originalDataTypeProvider = originalDataTypeProvider;
    }

    @Override
    public String getCanonicalDataTypeName(Column column, Table table) {
        String dataTypeName = originalDataTypeProvider.getCanonicalDataTypeName(column, table);
        return OBJECT_TYPE_BY_PRIMITIVE_TYPE.keySet().contains(dataTypeName) ? OBJECT_TYPE_BY_PRIMITIVE_TYPE.get(dataTypeName).getCanonicalName() : dataTypeName;
    }
}
