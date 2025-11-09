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

package de.yamass.redg.generator.extractor.datatypeprovider.xml;

import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DefaultDataType;
import de.yamass.redg.schema.model.Table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class XmlFileDataTypeProviderTest {
    @Test
    void testDeserializeXml() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("XmlFileDataTypeProviderTest.xml");
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

        TypeMappings mappings = XmlFileDataTypeProvider.deserializeXml(reader);

        Assertions.assertEquals(mappings.getTableTypeMappings().size(), 1);
        TableTypeMapping tableTypeMapping = mappings.getTableTypeMappings().get(0);
        Assertions.assertEquals(tableTypeMapping.getTableName(), "MY_TABLE");
        Assertions.assertEquals(tableTypeMapping.getColumnTypeMappings().size(), 1);
        ColumnTypeMapping columnTypeMapping = tableTypeMapping.getColumnTypeMappings().get(0);
        Assertions.assertEquals(columnTypeMapping.getColumnName(), "MY_FLAG");
        Assertions.assertEquals(columnTypeMapping.getJavaType(), "java.lang.Boolean");

        Assertions.assertEquals(mappings.getDefaultTypeMappings().size(), 1);
        DefaultTypeMapping defaultTypeMapping = mappings.getDefaultTypeMappings().get(0);
        Assertions.assertEquals(defaultTypeMapping.getSqlType(), "DECIMAL(1)");
        Assertions.assertEquals(defaultTypeMapping.getJavaType(), "java.lang.Boolean");
    }

    @Test
    void testXStream() throws IOException {
        TypeMappings typeMappings = new TypeMappings();

        TableTypeMapping tableTypeMapping = new TableTypeMapping();
        tableTypeMapping.setTableName("MY_TABLE");
        ColumnTypeMapping columnTypeMapping = new ColumnTypeMapping();
        columnTypeMapping.setColumnName("MY_FLAG");
        columnTypeMapping.setJavaType("java.lang.Boolean");
        tableTypeMapping.setColumnTypeMappings(new ArrayList<>(Collections.singletonList(columnTypeMapping)));
        typeMappings.setTableTypeMappings(new ArrayList<>(Collections.singletonList(tableTypeMapping)));

        DefaultTypeMapping defaultTypeMapping = new DefaultTypeMapping();
        defaultTypeMapping.setSqlType("DECIMAL(1)");
        defaultTypeMapping.setJavaType("java.lang.Boolean");
        typeMappings.setDefaultTypeMappings(new ArrayList<>(Collections.singletonList(defaultTypeMapping)));

        String serializedConfig = XmlFileDataTypeProvider.createXStream().toXML(typeMappings);

        assertEqualsIgnoreXmlWhiteSpaces(readResource("XmlFileDataTypeProviderTest.xml"), serializedConfig);
    }

    @Test
    void testGetDataTypeByName() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setTableTypeMappings(Arrays.asList(
                new TableTypeMapping(".+", Collections.singletonList(new ColumnTypeMapping("ACTIVE", "java.lang.Boolean"))),
                new TableTypeMapping("JOIN_TABLE", Collections.singletonList(new ColumnTypeMapping(".*_ID", "java.math.BigDecimal")))
        ));
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assertions.assertEquals(dataTypeProvider.getDataTypeByName("FOO", "ACTIVE"), "java.lang.Boolean");
        Assertions.assertEquals(dataTypeProvider.getDataTypeByName("BAR", "ACTIVE"), "java.lang.Boolean");
        Assertions.assertEquals(dataTypeProvider.getDataTypeByName("BAR", "INACTIVE"), null);

        Assertions.assertEquals(dataTypeProvider.getDataTypeByName("JOIN_TABLE", "FOO_ID"), "java.math.BigDecimal");
        Assertions.assertEquals(dataTypeProvider.getDataTypeByName("JOIN_TABLE", "BAR"), null);
    }

    @Test
    void testGetDataTypeBySqlName() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setDefaultTypeMappings(Arrays.asList(
                new DefaultTypeMapping("DECIMAL", "java.lang.Long"),
                new DefaultTypeMapping("DECIMAL(1)", "java.lang.Boolean"),
                new DefaultTypeMapping(" TIMESTAMP  WITH    TIME ZONE ( 30 , 0 ) ", "java.sql.Timestamp")
        ));

        Table dummyTable = new de.yamass.redg.schema.model.MutableTable(null, "DUMMY_TABLE");
        
        // Column 1: DECIMAL(1)
        DefaultDataType decimal1Type = new DefaultDataType("DECIMAL", JDBCType.DECIMAL, JDBCType.DECIMAL.getVendorTypeNumber(), null, false, 0, 0, 0, 1, true, false);
        Column column1 = new Column("COL1", decimal1Type, true, false, dummyTable);

        // Column 2: DECIMAL(10)
        DefaultDataType decimal10Type = new DefaultDataType("DECIMAL", JDBCType.DECIMAL, JDBCType.DECIMAL.getVendorTypeNumber(), null, false, 0, 0, 0, 10, true, false);
        Column column2 = new Column("COL2", decimal10Type, true, false, dummyTable);

        // Column 3: TIMESTAMP WITH TIME ZONE(30, 0)
        DefaultDataType timestampType = new DefaultDataType("TIMESTAMP WITH TIME ZONE", JDBCType.TIMESTAMP_WITH_TIMEZONE, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), null, false, 0, 0, 0, 30, true, false);
        Column column3 = new Column("COL3", timestampType, true, false, dummyTable);

        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assertions.assertEquals("java.lang.Boolean", dataTypeProvider.getCanonicalDataTypeName(column1, dummyTable));
        Assertions.assertEquals("java.lang.Long", dataTypeProvider.getCanonicalDataTypeName(column2, dummyTable));
        Assertions.assertEquals("java.sql.Timestamp", dataTypeProvider.getCanonicalDataTypeName(column3, dummyTable));
    }

    @Test
    void testCanHandleMissingTableTypeMappings() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setDefaultTypeMappings(Collections.emptyList());
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assertions.assertEquals("java.math.BigDecimal", dataTypeProvider.getCanonicalDataTypeName(createColumnMock(), createTableMock()));
    }

    @Test
    void testCanHandleMissingDefaultTypeMappings() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setTableTypeMappings(Collections.emptyList());
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assertions.assertEquals("java.math.BigDecimal", dataTypeProvider.getCanonicalDataTypeName(createColumnMock(), createTableMock()));
    }

    private Column createColumnMock() {
        Table table = createTableMock();
        DefaultDataType dataType = new DefaultDataType("NUMBER", JDBCType.NUMERIC, JDBCType.NUMERIC.getVendorTypeNumber(), null, false, 0, 10, 0, 23, true, false);
        return new Column("MY_COLUMN", dataType, true, false, table);
    }
    
    private Table createTableMock() {
        return new de.yamass.redg.schema.model.MutableTable(null, "MY_TABLE");
    }

    private void assertEqualsIgnoreXmlWhiteSpaces(String expected, String actual) {
        expected = expected.replaceAll("[ \\t]+", " ");
        actual = actual.replaceAll("[ \\t]+", " ");
        expected = expected.replaceAll("[\\r\\n]+", "");
        actual = actual.replaceAll("[\\r\\n]+", " ");
        expected = expected.replaceAll("\\s+", "");
        actual = actual.replaceAll("\\s+", "");
        Assertions.assertEquals(expected, actual);
    }

    private String readResource(String resourceName) throws IOException {
        InputStream stream = this.getClass().getResourceAsStream(resourceName);
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

}