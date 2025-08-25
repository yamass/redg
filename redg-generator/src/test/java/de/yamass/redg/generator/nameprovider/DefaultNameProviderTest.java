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

import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DefaultNameProviderTest {

    @Test
    public void testClassNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        assertEquals("WebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("WEB_USER")));
        assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER")));
        assertEquals("DemoWebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("DEMO_WEB_USER")));
        assertEquals("DemoWebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("DEMO_WEB_USER")));
        assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("123USER")));
        assertEquals("User123", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER123")));
        assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("123_USER")));
        assertEquals("User123", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER_123")));
        assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USERÜÖÄ")));
        assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("ÜÖÄ_USER")));
        assertEquals("UserTest", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER_ÜÖÄ_TEST")));
    }

    @Test
    public void testMethodNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_NAME", null)));
        assertEquals("lastname", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LASTNAME", null)));
        assertEquals("last123Name", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_123_NAME", null)));
        assertEquals("last123name", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_123NAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("123_LAST_NAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("123LAST_NAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_ÜÄÖ_NAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_ÜÄÖNAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("ÜÄÖ_LAST_NAME", null)));
        assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("ÜÄÖLAST_NAME", null)));
    }

    @Test
    public void testForeignKeyNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        assertEquals("creatorUser",
                provider.getMethodNameForReference(DummyDatabaseStructureProvider.getSimpleForeignKey("creator", "X", "User")));

        assertEquals("blogPostCreator",
                provider.getMethodNameForReference(
                        DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User")));
    }

    @Test
    public void testGetMethodNameForIncomingForeignKey() {
        DefaultNameProvider provider = new DefaultNameProvider();
        assertEquals("referencingsForCreatorUser",
                provider.getMethodNameForIncomingForeignKey(
                        DummyDatabaseStructureProvider.getSimpleForeignKey("creator", "Referencing", "User")));

        assertEquals("usersForBlogPostCreator",
                provider.getMethodNameForIncomingForeignKey(
                        DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User")));
    }


}
