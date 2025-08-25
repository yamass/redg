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

package de.yamass.redg.runtime;

import java.util.List;

/**
 * This is the interface for every entity that can be inserted into a database by RedG. Every generated entity class implements this interface
 */
public interface RedGEntity {

    /**
     * Returns a valid SQL insert statement that can be used to persist this entity in the database.
     * @return A valid SQL statement
     */
    String getSQLString();

    /**
     * Returns a valid SQL statement using '?' as placeholders for values that will be provided by the {@link #getPreparedStatementValues()} method.
     * @return A valid SQL statement with placeholders
     * @see java.sql.PreparedStatement
     */
    String getPreparedStatementString();

    /**
     * Returns an array of objects that contains the values for the placeholders specified in {@link #getPreparedStatementString()}.
     * @return The array of values for the placeholders
     * @see java.sql.PreparedStatement#setObject(int, Object, int)
     */
    Object[] getPreparedStatementValues();

    /**
     * Returns an array of the sql types for the objects from {@link #getPreparedStatementValues()}. Has to be the same size as the object array
     * @return The array of sql data type integers
     * @see java.sql.SQLType
     * @see java.sql.PreparedStatement#setObject(int, Object, int)
     */
    AttributeMetaInfo[] getPreparedStatementValuesMetaInfos();

    /**
     * Returns a list of all other {@link RedGEntity}s this entity depends on (has foreign keys to).
     * @return A {@link List} of {@link RedGEntity}s this entity depends on
     */
    List<RedGEntity> getDependencies();
}
