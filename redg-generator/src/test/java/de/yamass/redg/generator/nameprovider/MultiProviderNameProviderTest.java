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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MultiProviderNameProviderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final Table webUser = DummyDatabaseStructureProvider.getDummyTable("WEB_USER");
    private final Table webTransactions = DummyDatabaseStructureProvider.getDummyTable("WEB_TRANSACTIONS");

    private final Column idColumn = DummyDatabaseStructureProvider.getDummyColumn("ID", null);
    private final Column nameColumn = DummyDatabaseStructureProvider.getDummyColumn("FIRST_NAME", null);

    private final ForeignKey userFk = DummyDatabaseStructureProvider.getSimpleForeignKey("CREATOR", "X", "USER");
    private final ForeignKey blogFk = DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User");

    @Test
    public void testAppendProviderAfterUsage() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("providers after a name");

        MultiProviderNameProvider provider = new MultiProviderNameProvider();
        provider.getClassNameForTable(webUser);
        NameProvider extraProvider = mock(NameProvider.class);
        provider.appendProvider(extraProvider);
    }

    @Test
    public void testPrependProviderAfterUsage() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("providers after a name");

        MultiProviderNameProvider provider = new MultiProviderNameProvider();
        provider.getClassNameForTable(webUser);
        NameProvider extraProvider = mock(NameProvider.class);
        provider.prependProvider(extraProvider);
    }



    @Test
    public void testClassNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getClassNameForTable(same(webUser))).thenReturn("TestName");
        when(extraProvider.getClassNameForTable(same(webTransactions))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("TestName", provider.getClassNameForTable(webUser));
        assertEquals("WebTransactions", provider.getClassNameForTable(webTransactions));

    }

    @Test
    public void testMethodNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getMethodNameForColumn(same(idColumn))).thenReturn("Identification");
        when(extraProvider.getMethodNameForColumn(same(nameColumn))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("Identification", provider.getMethodNameForColumn(idColumn));
        assertEquals("firstName", provider.getMethodNameForColumn(nameColumn));

    }

    @Test
    public void testForeignKeyNameGeneration() {
        MultiProviderNameProvider provider = new MultiProviderNameProvider();

        NameProvider extraProvider = mock(NameProvider.class);

        when(extraProvider.getMethodNameForReference(same(userFk))).thenReturn("creator");
        when(extraProvider.getMethodNameForReference(same(blogFk))).thenReturn(null);
        provider.appendProvider(extraProvider);

        assertEquals("creator", provider.getMethodNameForReference(userFk));
        assertEquals("blogPostCreator", provider.getMethodNameForReference(blogFk));

    }
}
