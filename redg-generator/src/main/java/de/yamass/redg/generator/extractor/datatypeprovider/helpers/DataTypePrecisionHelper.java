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

package de.yamass.redg.generator.extractor.datatypeprovider.helpers;

import schemacrawler.schema.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to get all precision variants of a SQL data type.
 */
public class DataTypePrecisionHelper {

    /**
     * Gets all possible precision variants of the sQL data type. For a NUMBER(5,4) it returns
     * <ul>
     *     <li>NUMBER(5,4)</li>
     *     <li>NUMBER(5)</li>
     *     <li>NUMBER</li>
     * </ul>
     * @param column The column data
     * @return A list with all 3 variants
     */
    public static List<String> getDataTypeWithAllPrecisionVariants(final Column column) {
        final ArrayList<String> list = new ArrayList<>(3);
        list.add(String.format("%s(%d,%d)", column.getColumnDataType().getName(), column.getSize(), column.getDecimalDigits()));
        list.add(String.format("%s(%d)", column.getColumnDataType().getName(), column.getSize()));
        list.add(column.getColumnDataType().getName());
        return list;
    }
}
