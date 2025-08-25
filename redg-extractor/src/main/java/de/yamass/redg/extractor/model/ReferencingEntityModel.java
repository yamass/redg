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

package de.yamass.redg.extractor.model;

import java.util.List;

/**
 * This is an {@link EntityModel} that basically contains a reference to a complete {@link EntityModel}. It should be used to reference entities that might not
 * be in the entity list but are referenced by foreign keys by some known entity.
 */
public class ReferencingEntityModel extends EntityModel {

    private String typeName;

    public ReferencingEntityModel(final String typeName) {
        super(null,null);
        this.typeName = typeName;
    }

    @Override
    public void setVariableName(final String variableName) {
        throw new UnsupportedOperationException("This is a referencing entity model.");
    }

    @Override
    public List<EntityModel> getNotNullRefs() {
        throw new UnsupportedOperationException("This is a referencing entity model.");
    }

    @Override
    public void setNotNullRefs(final List<EntityModel> notNullRefs) {
        throw new UnsupportedOperationException("This is a referencing entity model.");
    }

    @Override
    public void addNotNullRef(final EntityModel entityModel) {
        throw new UnsupportedOperationException("This is a referencing entity model.");
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }
}
