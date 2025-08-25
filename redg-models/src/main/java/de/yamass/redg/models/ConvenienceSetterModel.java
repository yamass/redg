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

public class ConvenienceSetterModel implements Serializable{

    private String setterJavaTypeName;
    private String fullyQualifiedConverterMethodName;

    public ConvenienceSetterModel(String setterJavaTypeName, String fullyQualifiedConverterMethodName) {
        this.setterJavaTypeName = setterJavaTypeName;
        this.fullyQualifiedConverterMethodName = fullyQualifiedConverterMethodName;
    }

    public String getSetterJavaTypeName() {
        return setterJavaTypeName;
    }

    public void setSetterJavaTypeName(String setterJavaTypeName) {
        this.setterJavaTypeName = setterJavaTypeName;
    }

    public String getFullyQualifiedConverterMethodName() {
        return fullyQualifiedConverterMethodName;
    }

    public void setFullyQualifiedConverterMethodName(String fullyQualifiedConverterMethodName) {
        this.fullyQualifiedConverterMethodName = fullyQualifiedConverterMethodName;
    }

    public boolean isPrimitiveType() {
        return ModelUtil.PRIMITIVE_TYPE_NAMES.contains(setterJavaTypeName);
    }
}
