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

import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.JDBCType;

class NoPrimitiveTypesDataTypeProviderWrapperTest {
    private static final Table DUMMY_TABLE = new de.yamass.redg.schema.model.MutableTable(null, "DUMMY_TABLE");
    private static final Column DUMMY_COLUMN = new Column("DUMMY_COL", new DefaultDataType("VARCHAR", JDBCType.VARCHAR, JDBCType.VARCHAR.getVendorTypeNumber(), null, false, 0), true, false, DUMMY_TABLE);
    
    @Test
    void getDataTypeBoolean() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "boolean");
        Assertions.assertEquals("java.lang.Boolean", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeCharacter() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "char");
        Assertions.assertEquals("java.lang.Character", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeByte() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "byte");
        Assertions.assertEquals("java.lang.Byte", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeShort() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "short");
        Assertions.assertEquals("java.lang.Short", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeInteger() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "int");
        Assertions.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeLong() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "long");
        Assertions.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeFloat() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "float");
        Assertions.assertEquals("java.lang.Float", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeDouble() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "double");
        Assertions.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

    @Test
    void getDataTypeNonPrimitiveType() throws Exception {
        NoPrimitiveTypesDataTypeProviderWrapper provider = new NoPrimitiveTypesDataTypeProviderWrapper((column, table) -> "java.lang.Double");
        Assertions.assertEquals("java.lang.Double", provider.getCanonicalDataTypeName(DUMMY_COLUMN, DUMMY_TABLE));
    }

}