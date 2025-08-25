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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Model for usage in the stringtemplate template
 */
public class TableModel implements Serializable {

    private String name;
    /**
     * The prefixed name.
     */
    private String className;
    private String sqlName;
    private String sqlFullName;
    private String packageName;

    private boolean hasColumnsAndForeignKeys = false;

    private List<ForeignKeyModel> foreignKeys = new ArrayList<>();
    private List<IncomingForeignKeyModel> incomingForeignKeys = new ArrayList<>();
    private List<ColumnModel> columns = new ArrayList<>();

    private Map<String, JoinTableSimplifierModel> joinTableSimplifierData = new HashMap<>();

    public TableModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public Collection<ForeignKeyModel> getForeignKeys() {
        return foreignKeys;
    }

    public Collection<ForeignKeyModel> getNullableForeignKeys() {
        return foreignKeys.stream().filter(fkm -> !fkm.isNotNull()).collect(Collectors.toList());
    }

    public Collection<ForeignKeyModel> getNotNullForeignKeys() {
        return foreignKeys.stream().filter(ForeignKeyModel::isNotNull).collect(Collectors.toList());
    }

    public void setForeignKeys(final List<ForeignKeyModel> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public ColumnModel getColumnByName(final String name) {
        return columns.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst().orElse(null);
    }

    public ColumnModel getColumnBySQLName(final String name) {
        return columns.stream()
                .filter(c -> c.getDbName().equals(name))
                .findFirst().orElse(null);
    }

    public void setColumns(final List<ColumnModel> columns) {
        this.columns = columns;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(final String sqlName) {
        this.sqlName = sqlName;
    }

    public String getSqlFullName() {
        return sqlFullName;
    }

    public void setSqlFullName(final String sqlFullName) {
        this.sqlFullName = sqlFullName;
    }

    public boolean hasColumnsAndForeignKeys() {
        return hasColumnsAndForeignKeys;
    }

    public void setHasColumnsAndForeignKeys(final boolean hasColumnsAndForeignKeys) {
        this.hasColumnsAndForeignKeys = hasColumnsAndForeignKeys;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public Map<String, JoinTableSimplifierModel> getJoinTableSimplifierData() {
        return joinTableSimplifierData;
    }

    public void setJoinTableSimplifierData(final Map<String, JoinTableSimplifierModel> joinTableSimplifierData) {
        this.joinTableSimplifierData = joinTableSimplifierData;
    }

    public List<IncomingForeignKeyModel> getIncomingForeignKeys() {
        return incomingForeignKeys;
    }

    public void setIncomingForeignKeys(final List<IncomingForeignKeyModel> incomingForeignKeys) {
        this.incomingForeignKeys = incomingForeignKeys;
    }

    public List<IncomingForeignKeyModel> getNullableIncomingForeignKeys() {
        return incomingForeignKeys.stream()
                .filter(fk -> !fk.isNotNull())
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getPrimaryKeyColumns() {
        return columns.stream()
                .filter(ColumnModel::isPartOfPrimaryKey)
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getForeignKeyColumns() {
        return columns.stream()
                .filter(ColumnModel::isPartOfForeignKey)
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getNonPrimaryKeyColumns() {
        return columns.stream()
                .filter(c -> !c.isPartOfPrimaryKey())
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getNonPrimaryKeyNonFKColumns() {
        return columns.stream()
                .filter(c -> !c.isPartOfPrimaryKey())
                .filter(c -> !c.isPartOfForeignKey())
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getNonForeignKeyColumns() {
        return columns.stream()
                .filter(c -> !c.isPartOfForeignKey())
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getExplicitAttributes() {
        return columns.stream()
                .filter(ColumnModel::isExplicitAttribute)
                .collect(Collectors.toList());
    }

    public List<ColumnModel> getNonExplicitAttributes() {
        return columns.stream()
                .filter((columnModel) -> !columnModel.isExplicitAttribute())
                .collect(Collectors.toList());
    }
    public List<ColumnModel> getNonExplicitNonFKAttributes() {
        return columns.stream()
                .filter((columnModel) -> !columnModel.isExplicitAttribute())
                .filter(c -> !c.isPartOfForeignKey())
                .collect(Collectors.toList());
    }
}
