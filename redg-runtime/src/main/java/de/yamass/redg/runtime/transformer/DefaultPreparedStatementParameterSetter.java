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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import de.yamass.redg.runtime.AttributeMetaInfo;

/**
 * Default transformer that does no transformation. It simply outputs the input.
 */
public class DefaultPreparedStatementParameterSetter implements PreparedStatementParameterSetter {

    private static final List<Integer> STRING_SQL_TYPES = Arrays.asList(Types.CHAR, Types.VARCHAR, Types.LONGNVARCHAR);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameter(PreparedStatement statement, int parameterIndex, final Object o, final AttributeMetaInfo attributeMetaInfo, final Connection connection) throws SQLException {
        if (!(o instanceof String) && STRING_SQL_TYPES.contains(attributeMetaInfo.getSqlTypeInt())) {
            statement.setObject(parameterIndex, o.toString(), attributeMetaInfo.getSqlTypeInt());
        } else {
            statement.setObject(parameterIndex, o, attributeMetaInfo.getSqlTypeInt());
        }
    }
}
