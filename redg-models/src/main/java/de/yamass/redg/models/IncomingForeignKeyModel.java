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


public class IncomingForeignKeyModel implements Serializable{

    private String referencingJavaTypeName;
    private String referencingAttributeName;
    private String attributeName;
    private boolean notNull;

    public String getReferencingJavaTypeName() {
        return referencingJavaTypeName;
    }

    public void setReferencingJavaTypeName(final String referencingJavaTypeName) {
        this.referencingJavaTypeName = referencingJavaTypeName;
    }

    public String getReferencingAttributeName() {
        return referencingAttributeName;
    }

    public void setReferencingAttributeName(String referencingAttributeName) {
        this.referencingAttributeName = referencingAttributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isNotNull() {
        return notNull;
    }
}
