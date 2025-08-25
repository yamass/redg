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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

public class XmlFileDataTypeProviderTest {
    @Test
    public void testDeserializeXml() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("XmlFileDataTypeProviderTest.xml");
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

        TypeMappings mappings = XmlFileDataTypeProvider.deserializeXml(reader);

        Assert.assertEquals(mappings.getTableTypeMappings().size(), 1);
        TableTypeMapping tableTypeMapping = mappings.getTableTypeMappings().get(0);
        Assert.assertEquals(tableTypeMapping.getTableName(), "MY_TABLE");
        Assert.assertEquals(tableTypeMapping.getColumnTypeMappings().size(), 1);
        ColumnTypeMapping columnTypeMapping = tableTypeMapping.getColumnTypeMappings().get(0);
        Assert.assertEquals(columnTypeMapping.getColumnName(), "MY_FLAG");
        Assert.assertEquals(columnTypeMapping.getJavaType(), "java.lang.Boolean");

        Assert.assertEquals(mappings.getDefaultTypeMappings().size(), 1);
        DefaultTypeMapping defaultTypeMapping = mappings.getDefaultTypeMappings().get(0);
        Assert.assertEquals(defaultTypeMapping.getSqlType(), "DECIMAL(1)");
        Assert.assertEquals(defaultTypeMapping.getJavaType(), "java.lang.Boolean");
    }

    @Test
    public void testXStream() throws IOException {
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
    public void testGetDataTypeByName() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setTableTypeMappings(Arrays.asList(
                new TableTypeMapping(".+", Collections.singletonList(new ColumnTypeMapping("ACTIVE", "java.lang.Boolean"))),
                new TableTypeMapping("JOIN_TABLE", Collections.singletonList(new ColumnTypeMapping(".*_ID", "java.math.BigDecimal")))
        ));
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assert.assertEquals(dataTypeProvider.getDataTypeByName("FOO", "ACTIVE"), "java.lang.Boolean");
        Assert.assertEquals(dataTypeProvider.getDataTypeByName("BAR", "ACTIVE"), "java.lang.Boolean");
        Assert.assertEquals(dataTypeProvider.getDataTypeByName("BAR", "INACTIVE"), null);

        Assert.assertEquals(dataTypeProvider.getDataTypeByName("JOIN_TABLE", "FOO_ID"), "java.math.BigDecimal");
        Assert.assertEquals(dataTypeProvider.getDataTypeByName("JOIN_TABLE", "BAR"), null);
    }

    @Test
    public void testGetDataTypeBySqlName() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setDefaultTypeMappings(Arrays.asList(
                new DefaultTypeMapping("DECIMAL", "java.lang.Long"),
                new DefaultTypeMapping("DECIMAL(1)", "java.lang.Boolean"),
                new DefaultTypeMapping(" TIMESTAMP  WITH    TIME ZONE ( 30 , 0 ) ", "java.sql.Timestamp")
        ));

        ColumnDataType cdt = Mockito.mock(ColumnDataType.class);
        when(cdt.getName()).thenReturn("DECIMAL");
        Column column1 = Mockito.mock(Column.class);
        when(column1.getColumnDataType()).thenReturn(cdt);
        when(column1.getSize()).thenReturn(1);
        when(column1.getDecimalDigits()).thenReturn(0);

        Column column2 = Mockito.mock(Column.class);
        when(column2.getColumnDataType()).thenReturn(cdt);
        when(column2.getSize()).thenReturn(10);
        when(column2.getDecimalDigits()).thenReturn(0);

        ColumnDataType timestampWithTimeZoneDataType = Mockito.mock(ColumnDataType.class);
        when(timestampWithTimeZoneDataType.getName()).thenReturn("TIMESTAMP WITH TIME ZONE");
        Column column3 = Mockito.mock(Column.class);
        when(column3.getColumnDataType()).thenReturn(timestampWithTimeZoneDataType);
        when(column3.getSize()).thenReturn(30);
        when(column3.getDecimalDigits()).thenReturn(0);

        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assert.assertEquals("java.lang.Boolean", dataTypeProvider.getDataTypeBySqlType(column1));
        Assert.assertEquals("java.lang.Long", dataTypeProvider.getDataTypeBySqlType(column2));
        Assert.assertEquals("java.sql.Timestamp", dataTypeProvider.getDataTypeBySqlType(column3));
    }

    @Test
    public void testCanHandleMissingTableTypeMappings() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setDefaultTypeMappings(Collections.emptyList());
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assert.assertEquals("java.lang.Integer", dataTypeProvider.getCanonicalDataTypeName(createColumnMock()));
    }

    @Test
    public void testCanHandleMissingDefaultTypeMappings() throws Exception {
        TypeMappings typeMappings = new TypeMappings();
        typeMappings.setTableTypeMappings(Collections.emptyList());
        XmlFileDataTypeProvider dataTypeProvider = new XmlFileDataTypeProvider(typeMappings, new DefaultDataTypeProvider());

        Assert.assertEquals("java.lang.Integer", dataTypeProvider.getCanonicalDataTypeName(createColumnMock()));
    }

    private Column createColumnMock() {
        Column columnMock = Mockito.mock(Column.class);
        when(columnMock.getName()).thenReturn("MY_COLUMN");
        ColumnDataType columnDataTypeMock = Mockito.mock(ColumnDataType.class);
        when(columnDataTypeMock.getName()).thenReturn("NUMBER");
        when(columnMock.getColumnDataType()).thenReturn(columnDataTypeMock);
        when(columnMock.getColumnDataType()).thenReturn(columnDataTypeMock);
        when(columnDataTypeMock.getTypeMappedClass()).then(invocationOnMock -> Integer.class);
        when(columnMock.getSize()).thenReturn(23);
        when(columnMock.getDecimalDigits()).thenReturn(10);
        Table tableMock = Mockito.mock(Table.class);
        when(tableMock.getName()).thenReturn("MY_TABLE");
        when(columnMock.getParent()).thenReturn(tableMock);
        return columnMock;
    }

    private void assertEqualsIgnoreXmlWhiteSpaces(String expected, String actual) {
        expected = expected.replaceAll("[ \\t]+", " ");
        actual = actual.replaceAll("[ \\t]+", " ");
        expected = expected.replaceAll("[\\r\\n]+", "");
        actual = actual.replaceAll("[\\r\\n]+", " ");
        expected = expected.replaceAll("\\s+", "");
        actual = actual.replaceAll("\\s+", "");
        Assert.assertEquals(expected, actual);
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