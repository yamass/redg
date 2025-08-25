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

package de.yamass.redg.runtime.visualization;

import java.util.LinkedList;
import java.util.List;

public class RedGVisualization {
    private String version;
    private List<RedGVisualizationObject> objects;
    private List<RedGVisualizationRelation> relationships;

    public RedGVisualization() {
        this.version = "1.0.0";
        this.objects = new LinkedList<>();
        this.relationships = new LinkedList<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public List<RedGVisualizationObject> getObjects() {
        return objects;
    }

    public void setObjects(final List<RedGVisualizationObject> objects) {
        this.objects = objects;
    }

    public List<RedGVisualizationRelation> getRelationships() {
        return relationships;
    }

    public void setRelationships(final List<RedGVisualizationRelation> relationships) {
        this.relationships = relationships;
    }
}
