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

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import schemacrawler.schema.Column;

public class XmlFileConvenienceSetterProviderTest {
    @Test
    public void getConvenienceSetters() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("XmlFileConvencienceSetterProviderTest.xml");
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

        XmlFileConvenienceSetterProvider provider = new XmlFileConvenienceSetterProvider(reader);

        Assert.assertEquals(1, provider.getConvenienceSetters(Mockito.mock(Column.class), "java.util.Date").size());
    }

}