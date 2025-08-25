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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("typeMappings")
public class TypeMappings {

    private List<TableTypeMapping> tableTypeMappings;
    private List<DefaultTypeMapping> defaultTypeMappings;

    public List<TableTypeMapping> getTableTypeMappings() {
        return tableTypeMappings;
    }

    public void setTableTypeMappings(List<TableTypeMapping> tableTypeMappings) {
        this.tableTypeMappings = tableTypeMappings;
    }

    public List<DefaultTypeMapping> getDefaultTypeMappings() {
        return defaultTypeMappings;
    }

    public void setDefaultTypeMappings(List<DefaultTypeMapping> defaultTypeMappings) {
        this.defaultTypeMappings = defaultTypeMappings;
    }
}
