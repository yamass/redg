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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import schemacrawler.schema.Column;

class NoPrimitiveTypesDataTypeProviderWrapperTest {
    @Test
    void getDataTypeBoolean() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "boolean");
        Assertions.assertEquals("java.lang.Boolean", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeCharacter() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "char");
        Assertions.assertEquals("java.lang.Character", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeByte() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "byte");
        Assertions.assertEquals("java.lang.Byte", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeShort() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "short");
        Assertions.assertEquals("java.lang.Short", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeInteger() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "int");
        Assertions.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeLong() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "long");
        Assertions.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeFloat() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "float");
        Assertions.assertEquals("java.lang.Float", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeDouble() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "double");
        Assertions.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    void getDataTypeNonPrimitiveType() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "java.lang.Double");
        Assertions.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

}