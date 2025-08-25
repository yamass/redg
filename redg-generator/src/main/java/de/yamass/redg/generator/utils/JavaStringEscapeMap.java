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

package de.yamass.redg.generator.utils;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;

/**
 * This Map dynamically transforms Strings and escapes double-quotes. It should be used with the StringTemplate template to escape all SQL identifiers.
 *
 * Based on the concept by @sharwell from https://github.com/antlr/stringtemplate4/issues/44
 */
public class JavaStringEscapeMap extends AbstractMap<String, Object> {

    @Override
    public Object get(Object key) {
        if (key instanceof String) {
            String str = (String) key;
            return str.replace("\"", "\\\"");
        }

        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return (key instanceof String);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
    }
}
