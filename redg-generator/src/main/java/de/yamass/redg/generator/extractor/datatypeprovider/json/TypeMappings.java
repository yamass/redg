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

package de.yamass.redg.generator.extractor.datatypeprovider.json;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The JSON type mapping definition for mapping with Jackson
 */
public class TypeMappings {

    private HashMap<String, HashMap<String, String>> tableMappings;
    private HashMap<String, String> defaultTypeMappings;


    public HashMap<String, HashMap<String, String>> getTableMappings() {
        return tableMappings;
    }

    public void setTableMappings(final HashMap<String, HashMap<String, String>> tableMappings) {
        this.tableMappings = tableMappings;
    }

    public HashMap<String, String> getDefaultTypeMappings() {
        return defaultTypeMappings;
    }

    public void setDefaultTypeMappings(final HashMap<String, String> defaultTypeMappings) {
        this.defaultTypeMappings = (HashMap<String, String>) defaultTypeMappings.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().replaceAll("\\s", ""), Map.Entry::getValue));
    }
}
