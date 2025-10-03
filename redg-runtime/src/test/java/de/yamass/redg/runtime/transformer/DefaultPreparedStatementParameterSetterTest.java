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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.Types;


class DefaultPreparedStatementParameterSetterTest {

    @Test
    void testTransform() throws Exception {
        PreparedStatement preparedStatementMock = Mockito.mock(PreparedStatement.class);

        DefaultPreparedStatementParameterSetter parameterSetter = new DefaultPreparedStatementParameterSetter();
        parameterSetter.setParameter(preparedStatementMock, 1, "test", createMockAttributeMetaInfo(), null);

        Mockito.verify(preparedStatementMock).setObject(1, "test", Types.VARCHAR);

        parameterSetter.setParameter(preparedStatementMock, 1, 'a', createMockAttributeMetaInfo(), null);

        Mockito.verify(preparedStatementMock).setObject(1, "a", Types.VARCHAR);

        parameterSetter.setParameter(preparedStatementMock, 1, 10, createMockAttributeMetaInfo2(), null);

        Mockito.verify(preparedStatementMock).setObject(1, 10, Types.BIGINT);
    }

    private AttributeMetaInfo createMockAttributeMetaInfo() {
        return new AttributeMetaInfo("", "", "", "", Types.VARCHAR, String.class, false);
    }
    private AttributeMetaInfo createMockAttributeMetaInfo2() {
        return new AttributeMetaInfo("", "", "", "", Types.BIGINT, String.class, false);
    }
}
