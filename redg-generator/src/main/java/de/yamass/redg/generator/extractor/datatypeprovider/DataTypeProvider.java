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

import schemacrawler.schema.Column;

/**
 * Common interface for all data type providers, that are able to decide the java data type for a database column
 */
public interface DataTypeProvider {
    /**
     * Called by during the model data extraction process for each column that is being processed with the current column as the parameter.
     * This method has to decide on the data type that will later represent this column in the generated java code.
     * <p>
     * Information about the {@link schemacrawler.schema.Table} can be obtained by calling {@link Column#getParent()}.<p>
     * Information about the SQL Data type and what schemacrawler recommends can be obtained by calling {@link Column#getColumnDataType()}.
     * @param column The current column
     * @return The canonical name of the data type to use for the given column
     */
    String getCanonicalDataTypeName(Column column);
}
