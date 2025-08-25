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

package de.yamass.redg.generator.extractor.conveniencesetterprovider.xml;

import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.models.ConvenienceSetterModel;
import com.thoughtworks.xstream.XStream;
import schemacrawler.schema.Column;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XmlFileConvenienceSetterProvider implements ConvenienceSetterProvider {

    private ConvenienceSetterConfig convenienceSetterConfig;

    public XmlFileConvenienceSetterProvider(Reader xmlReader) throws IOException {
        this(deserializeXml(xmlReader));
    }

    public XmlFileConvenienceSetterProvider(ConvenienceSetterConfig convenienceSetterConfig) {
        this.convenienceSetterConfig = convenienceSetterConfig;
    }

    static ConvenienceSetterConfig deserializeXml(Reader xmlReader) {
        ConvenienceSetterConfig config;
        XStream xStream = createXStream();
        config = (ConvenienceSetterConfig) xStream.fromXML(xmlReader, new ConvenienceSetterConfig());
        return config;
    }

    static XStream createXStream() {
        XStream xStream = new XStream();
        xStream.processAnnotations(new Class[]{
                ConvenienceSetterConfig.class
        });
        xStream.alias("convenienceSetterConfig", ConvenienceSetterConfig.class);
        xStream.addImplicitCollection(ConvenienceSetterConfig.class, "convenienceSetterConfigs", "javaType", DataTypeConvenienceSetterConfig.class);
        xStream.addImplicitCollection(DataTypeConvenienceSetterConfig.class, "convenienceSetters", "convenienceSetter", ConvenienceSetterModel.class);
        xStream.addDefaultImplementation(ArrayList.class, List.class);
        xStream.aliasAttribute(DataTypeConvenienceSetterConfig.class, "javaDataTypeName", "name");
        xStream.useAttributeFor(ConvenienceSetterModel.class, "setterJavaTypeName");
        xStream.useAttributeFor(ConvenienceSetterModel.class, "fullyQualifiedConverterMethodName");
        return xStream;
    }

    @Override
    public List<ConvenienceSetterModel> getConvenienceSetters(Column column, String javaDataTypeName) {
        return this.convenienceSetterConfig.getConvenienceSetterConfigs().stream()
                .filter(dataTypeConvenienceSetterConfig -> dataTypeConvenienceSetterConfig.getJavaDataTypeName().equals(javaDataTypeName))
                .flatMap(dataTypeConvenienceSetterConfig -> dataTypeConvenienceSetterConfig.getConvenienceSetters().stream())
                .collect(Collectors.toList());
    }
}
