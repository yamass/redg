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

package de.yamass.redg.runtime.transformer;

import de.yamass.redg.runtime.AttributeMetaInfo;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface used to transform java objects before adding them to the prepared statement. Should be used to "reverse" custom data type mapping.
 */
@FunctionalInterface
public interface PreparedStatementParameterSetter {

    /**
     * Use this method to customize how RedG sets parameters on a prepared statement. The information about the attribute/column
     * is stored in attributeMetaInfo.
     *
     * @param statement         The statement to set the parameter on
     * @param parameterIndex    The parameter index
     * @param value             The value to transform, or null.
     * @param attributeMetaInfo Meta information about the attribute
     * @param connection        The JDBC connection. Can be used to create objects like {@link java.sql.Clob} or {@link java.sql.Blob}
     * @throws SQLException Gets thrown if some SQL error occurred
     */
    void setParameter(PreparedStatement statement, int parameterIndex, @Nullable Object value, AttributeMetaInfo attributeMetaInfo, final Connection connection) throws SQLException;
}
