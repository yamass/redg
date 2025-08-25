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

package de.yamass.redg.runtime.insertvalues;


public interface SQLValuesFormatter {

    /**
     * Formats a value so that it can be inserted into the extractor. This is used to deal with some of the weird SQL types and formatting
     * @param value The value that has to be formatted
     * @param sqlDataType The SQL data type
     * @param fullTableName The full table name, with schema
     * @param tableName The table name
     * @param columnName The column name
     * @param <T> The type of the value
     * @return The string that gets placed in the SQL INSERT statement
     */
    <T> String formatValue(T value, String sqlDataType, String fullTableName, String tableName, String columnName);
}
