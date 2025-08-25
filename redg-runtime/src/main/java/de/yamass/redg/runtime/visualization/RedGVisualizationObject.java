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

public class RedGVisualizationObject {

    private String id;
    private String type;
    private String sqlName;
    private boolean isExistingEntity;
    private boolean isDummy;
    private List<RedGVisualizationField> explicitFields;
    private List<RedGVisualizationField> implicitFields;

    public RedGVisualizationObject() {
        explicitFields = new LinkedList<>();
        implicitFields = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(final String sqlName) {
        this.sqlName = sqlName;
    }

    public boolean isExistingEntity() {
        return isExistingEntity;
    }

    public void setExistingEntity(final boolean existingEntity) {
        isExistingEntity = existingEntity;
    }

    public boolean isDummy() {
        return isDummy;
    }

    public void setDummy(final boolean dummy) {
        isDummy = dummy;
    }

    public List<RedGVisualizationField> getExplicitFields() {
        return explicitFields;
    }

    public void setExplicitFields(final List<RedGVisualizationField> explicitFields) {
        this.explicitFields = explicitFields;
    }

    public List<RedGVisualizationField> getImplicitFields() {
        return implicitFields;
    }

    public void setImplicitFields(final List<RedGVisualizationField> implicitFields) {
        this.implicitFields = implicitFields;
    }
}
