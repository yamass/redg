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

package de.yamass.redg.runtime.defaultvalues;


import de.yamass.redg.models.ColumnModel;

public interface DefaultValueStrategy {

    /**
     * Generates and returns a default value for a column in a table.
     *
     * @param columnModel The complete column model of the column that needs a default value
     * @param type        the required class
     * @param <T>         The type of the field that needs a default value
     * @return The default value for the field. {@code null} is allowed if {@code notNull == false}. This is not checked anywhere
     */
    <T> T getDefaultValue(ColumnModel columnModel, final Class<T> type);
}
