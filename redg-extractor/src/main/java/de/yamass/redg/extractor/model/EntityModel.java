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

import de.yamass.redg.extractor.model.entitynameprovider.DefaultEntityNameProvider;
import de.yamass.redg.extractor.model.entitynameprovider.EntityNameProvider;
import de.yamass.redg.models.TableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityModel {

    private static EntityNameProvider entityNameProvider = new DefaultEntityNameProvider();

    public static EntityNameProvider getEntityNameProvider() {
        return entityNameProvider;
    }

    public static void setEntityNameProvider(final EntityNameProvider entityNameProvider) {
        EntityModel.entityNameProvider = entityNameProvider;
    }

    private String variableName;

    private TableModel tableModel;

    private Map<String, ValueModel> values;

    private Map<String, EntityModel> nullableRefs;

    private List<EntityModel> notNullRefs;

    public EntityModel(final TableModel tableModel) {
        this(entityNameProvider.getName(tableModel), tableModel);
    }

    public EntityModel(final String variableName, final TableModel tableModel) {
        this.variableName = variableName;
        this.tableModel = tableModel;
        this.values = new HashMap<>();
        this.notNullRefs = new ArrayList<>();
        this.nullableRefs = new HashMap<>();
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(final String variableName) {
        this.variableName = variableName;
    }

    public Map<String, ValueModel> getValues() {
        return values;
    }

    public Map<String, ValueModel> getValuesWithoutFKs() {
        return values.entrySet().stream()
                .filter(e -> e.getValue().isNonFK())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void setValues(final Map<String, ValueModel> values) {
        this.values = values;
    }

    public void addValues(final String methodName, final ValueModel methodParameter) {
        this.values.put(methodName, methodParameter);
    }

    public List<EntityModel> getNotNullRefs() {
        return notNullRefs;
    }

    public void setNotNullRefs(final List<EntityModel> notNullRefs) {
        this.notNullRefs = notNullRefs;
    }

    public Map<String, EntityModel> getNullableRefs() {
        return nullableRefs;
    }

    public void setNullableRefs(final Map<String, EntityModel> nullableRefs) {
        this.nullableRefs = nullableRefs;
    }

    public void addNullableRef(final String name, final EntityModel entityModel) {
        this.nullableRefs.put(name, entityModel);
    }

    public void addNotNullRef(final EntityModel entityModel) {
        this.notNullRefs.add(entityModel);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public List<EntityModel> getAllRefs() {
        return Stream.concat(this.notNullRefs.stream(), this.nullableRefs.values().stream()).collect(Collectors.toList());
    }

    public static final class ValueModel {
        public enum ForeignKeyState {
            FK, NON_FK, UNKNOWN
        }

        private String value;
        private ForeignKeyState foreignKeyState;

        public ValueModel(final String value, final ForeignKeyState foreignKeyState) {
            this.value = value;
            this.foreignKeyState = foreignKeyState;
        }

        public String getValue() {
            return value;
        }

        public ForeignKeyState getForeignKeyState() {
            return foreignKeyState;
        }

        public boolean isNonFK() {
            return foreignKeyState != ForeignKeyState.FK;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ValueModel that = (ValueModel) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;
            return foreignKeyState == that.foreignKeyState;
        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + (foreignKeyState != null ? foreignKeyState.hashCode() : 0);
            return result;
        }
    }

    public boolean isGenerateAsNewEntity() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getTableModel().getSqlFullName())
                .append("[")
                .append(this.getValues().entrySet().stream()
                        .map((e) -> e.getKey() + " -> " + e.getValue().getValue())
                        .collect(Collectors.joining(", ")))
                .append("]");
        return builder.toString();
    }

}
