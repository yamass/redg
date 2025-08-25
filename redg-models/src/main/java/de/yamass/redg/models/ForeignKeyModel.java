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

package de.yamass.redg.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model for usage in the stringtemplate template
 */
public class ForeignKeyModel implements Serializable{

    private String javaTypeName;
    private String name;
    private Map<String, ForeignKeyColumnModel> references;

    private boolean notNull = true;

    public ForeignKeyModel() {
        this.references = new HashMap<>();
    }

    public String getJavaTypeName() {
        return javaTypeName;
    }

    public void setJavaTypeName(final String javaTypeName) {
        this.javaTypeName = javaTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, ForeignKeyColumnModel> getReferences() {
        return references;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(final boolean notNull) {
        this.notNull = notNull;
    }
}
