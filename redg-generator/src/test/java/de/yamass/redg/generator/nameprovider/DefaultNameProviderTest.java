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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class DefaultNameProviderTest {

    @Test
    void testClassNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        Assertions.assertEquals("WebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("WEB_USER")));
        Assertions.assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER")));
        Assertions.assertEquals("DemoWebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("DEMO_WEB_USER")));
        Assertions.assertEquals("DemoWebUser", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("DEMO_WEB_USER")));
        Assertions.assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("123USER")));
        Assertions.assertEquals("User123", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER123")));
        Assertions.assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("123_USER")));
        Assertions.assertEquals("User123", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER_123")));
        Assertions.assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USERÜÖÄ")));
        Assertions.assertEquals("User", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("ÜÖÄ_USER")));
        Assertions.assertEquals("UserTest", provider.getClassNameForTable(DummyDatabaseStructureProvider.getDummyTable("USER_ÜÖÄ_TEST")));
    }

    @Test
    void testMethodNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_NAME", null)));
        Assertions.assertEquals("lastname", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LASTNAME", null)));
        Assertions.assertEquals("last123Name", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_123_NAME", null)));
        Assertions.assertEquals("last123name", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_123NAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("123_LAST_NAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("123LAST_NAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_ÜÄÖ_NAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("LAST_ÜÄÖNAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("ÜÄÖ_LAST_NAME", null)));
        Assertions.assertEquals("lastName", provider.getMethodNameForColumn(DummyDatabaseStructureProvider.getDummyColumn("ÜÄÖLAST_NAME", null)));
    }

    @Test
    void testForeignKeyNameGeneration() {
        DefaultNameProvider provider = new DefaultNameProvider();
        Assertions.assertEquals("creatorUser", provider.getMethodNameForReference(DummyDatabaseStructureProvider.getSimpleForeignKey("creator", "X", "User")));

        Assertions.assertEquals("blogPostCreator", provider.getMethodNameForReference(
                DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User")));
    }

    @Test
    void testGetMethodNameForIncomingForeignKey() {
        DefaultNameProvider provider = new DefaultNameProvider();
        Assertions.assertEquals("referencingsForCreatorUser", provider.getMethodNameForIncomingForeignKey(
                DummyDatabaseStructureProvider.getSimpleForeignKey("creator", "Referencing", "User")));

        Assertions.assertEquals("usersForBlogPostCreator", provider.getMethodNameForIncomingForeignKey(
                DummyDatabaseStructureProvider.getMultiPartForeignKey("FK_BLOG_POST_CREATOR_USER", "User")));
    }


}
