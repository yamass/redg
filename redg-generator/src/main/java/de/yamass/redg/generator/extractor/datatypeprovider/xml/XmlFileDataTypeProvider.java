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

import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.helpers.DataTypePrecisionHelper;
import com.thoughtworks.xstream.XStream;
import schemacrawler.schema.Column;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * A data type provider that reads the types from a json file.
 * </p><p>
 * The format for the file is:
 * </p><p><blockquote><pre>
 * &lt;typeMappings&gt;
 *   &lt;tableTypeMappings&gt;
 *     &lt;table tableName="CUSTOMER"&gt;
 *       &lt;column columnName="DTYPE" javaType="example.CustomerType"/&gt;
 *     &lt;/table&gt;
 *   &lt;/tableTypeMappings&gt;
 *   &lt;defaultTypeMappings&gt;
 *     &lt;typeMapping sqlType="TIMESTAMP WITH TIME ZONE" javaType="java.sql.Timestamp"/&gt;
 *   &lt;/defaultTypeMappings&gt;
 * &lt;/typeMappings&gt;
 * </pre></blockquote>
 */
public class XmlFileDataTypeProvider implements DataTypeProvider {
    private TypeMappings typeMappings;
    private DataTypeProvider fallbackDataTypeProvider;

    public XmlFileDataTypeProvider(Reader xmlReader, DataTypeProvider fallbackDataTypeProvider) throws IOException {
        this(deserializeXml(xmlReader), fallbackDataTypeProvider);
    }

    public XmlFileDataTypeProvider(TypeMappings typeMappings, DataTypeProvider fallbackDataTypeProvider) {
        this.typeMappings = typeMappings;
        this.fallbackDataTypeProvider = fallbackDataTypeProvider;
    }

    static TypeMappings deserializeXml(Reader xmlReader) {
        TypeMappings typeMappings;
        XStream xStream = createXStream();
        typeMappings = (TypeMappings) xStream.fromXML(xmlReader, new TypeMappings());
        return typeMappings;
    }

    static XStream createXStream() {
        XStream xStream = new XStream();
        xStream.processAnnotations(new Class[]{
                ColumnTypeMapping.class,
                DefaultTypeMapping.class,
                TableTypeMapping.class,
                TypeMappings.class
        });
        xStream.useAttributeFor(TableTypeMapping.class, "tableName");
        xStream.addDefaultImplementation(ArrayList.class, List.class);
        return xStream;
    }

    @Override
    public String getCanonicalDataTypeName(final Column column) {
        String dataTypeByName = getDataTypeByName(column.getParent().getName(), column.getName());
        String dataTypeByType = getDataTypeBySqlType(column);
        return dataTypeByName != null ? dataTypeByName :
                dataTypeByType != null ? dataTypeByType : fallbackDataTypeProvider.getCanonicalDataTypeName(column);
    }

    String getDataTypeByName(String tableName, String columnName) {
        if (typeMappings.getTableTypeMappings() == null) {
            return null;
        }

        return typeMappings.getTableTypeMappings().stream()
                .filter(tableTypeMapping -> tableName.matches(tableTypeMapping.getTableName()))
                .flatMap(tableTypeMapping -> tableTypeMapping.getColumnTypeMappings().stream())
                .filter(columnTypeMapping -> columnName.matches(columnTypeMapping.getColumnName()))
                .findFirst()
                .map(ColumnTypeMapping::getJavaType)
                .orElse(null);
    }

    String getDataTypeBySqlType(final Column column) {
        if (typeMappings.getDefaultTypeMappings() == null) {
            return null;
        }

        List<String> variants = DataTypePrecisionHelper.getDataTypeWithAllPrecisionVariants(column);
        return variants.stream()
                .map(variant ->
                        typeMappings.getDefaultTypeMappings().stream()
                                .filter(dtm -> dtm.getSqlType()
                                        .trim()
                                        .replaceAll("\\s+", " ") // normalize multiple space characters
                                        .replaceAll("\\s+(?=[(),])", "") // remove leading spaces before '(', ')' and ','
                                        .replaceAll("(?<=[(),])\\s+", "") // remove trailing spaces after '(', ')' and ','
                                        .toUpperCase().equals(variant.toUpperCase()))
                                .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(DefaultTypeMapping::getJavaType)
                .findFirst().orElse(null);
    }
}
