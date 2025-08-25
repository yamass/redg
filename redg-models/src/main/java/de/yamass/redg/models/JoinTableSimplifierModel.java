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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JoinTableSimplifierModel implements Serializable{

    private List<String> constructorParams;
    private Map<String, String> methodParams;
    private String name;

    public JoinTableSimplifierModel() {
        this.constructorParams = new ArrayList<>();
        this.methodParams = new HashMap<>();
    }

    public List<String> getConstructorParams() {
        return constructorParams;
    }

    public void setConstructorParams(final List<String> constructorParams) {
        this.constructorParams = constructorParams;
    }

    public Map<String, String> getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(final Map<String, String> methodParams) {
        this.methodParams = methodParams;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
