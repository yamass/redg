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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import schemacrawler.schema.Column;

public class NoPrimitiveTypesDataTypeProviderWrapperTest {
    @Test
    public void getDataTypeBoolean() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "boolean");
        Assert.assertEquals("java.lang.Boolean", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeCharacter() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "char");
        Assert.assertEquals("java.lang.Character", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeByte() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "byte");
        Assert.assertEquals("java.lang.Byte", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeShort() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "short");
        Assert.assertEquals("java.lang.Short", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeInteger() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "int");
        Assert.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeLong() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "long");
        Assert.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeFloat() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "float");
        Assert.assertEquals("java.lang.Float", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeDouble() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "double");
        Assert.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

    @Test
    public void getDataTypeNonPrimitiveType() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper(column -> "java.lang.Double");
        Assert.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(Mockito.mock(Column.class)));
    }

}