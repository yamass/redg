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

package de.yamass.redg.generator.nameprovider;

import de.yamass.redg.generator.extractor.nameprovider.MultiProviderNameProvider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MultiProviderNameProviderTest {

    private final Table webUser = DummyDatabaseStructureProvider.getDummyTable("WEB_USER");
    private final Table webTransactions = DummyDatabaseStructureProvider.getDummyTable("WEB_TRANSACTIONS");

    private final Column idColumn = DummyDatabaseStructureProvider.getDummyColumn("ID", null);
    private final Column nameColumn = DummyDatabaseStructureProvider.getDummyColumn("FIRST_NAME", null);

    private final ForeignKey userFk = DummyDatabaseStructureProvider.getSimpleForeignKey("CREATOR", "X", "USER");
    private final ForeignKey blogFk = DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User");

    @Test
    void testAppendProviderAfterUsage() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();
        provider.getClassNameForTable(webUser);
        NameProvider extraProvider = mock(NameProvider.class);
        assertThatThrownBy(() -> provider.appendProvider(extraProvider))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("providers after a name");
    }

    @Test
    void testPrependProviderAfterUsage() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();
        provider.getClassNameForTable(webUser);
        NameProvider extraProvider = mock(NameProvider.class);

        assertThatThrownBy(() -> provider.prependProvider(extraProvider))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("providers after a name");
    }

    @Test
    void testClassNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getClassNameForTable(same(webUser))).thenReturn("TestName");
        when(extraProvider.getClassNameForTable(same(webTransactions))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("TestName", provider.getClassNameForTable(webUser));
        assertEquals("WebTransactions", provider.getClassNameForTable(webTransactions));

    }

    @Test
    void testMethodNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getMethodNameForColumn(same(idColumn), same(webUser))).thenReturn("Identification");
        when(extraProvider.getMethodNameForColumn(same(nameColumn), same(webUser))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("Identification", provider.getMethodNameForColumn(idColumn, webUser));
        assertEquals("firstName", provider.getMethodNameForColumn(nameColumn, webUser));

    }

    @Test
    void testForeignKeyNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getMethodNameForReference(same(userFk))).thenReturn("creator");
        when(extraProvider.getMethodNameForReference(same(blogFk))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("creator", provider.getMethodNameForReference(userFk));
        assertEquals("blogPostCreatorUser", provider.getMethodNameForReference(blogFk));

    }
}
