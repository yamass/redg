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
import java.util.List;
import java.util.Map;

/**
 * Model for usage in the stringtemplate template
 */
public class ColumnModel implements Serializable{

    private String name;
    private String dbName;
    private String dbTableName;
    private String dbFullTableName;
    private String sqlType;
    private String javaTypeName;
    private int sqlTypeInt;
    private boolean notNull;
    private boolean partOfPrimaryKey;
    private boolean partOfForeignKey;
    private boolean explicitAttribute;
    private boolean unique;
    private List<ConvenienceSetterModel> convenienceSetters;

    private static final Map<String, Class<?>> primitiveMap = new HashMap<>();

    static {
        primitiveMap.put("byte", byte.class);
        primitiveMap.put("short", short.class);
        primitiveMap.put("int", int.class);
        primitiveMap.put("long", long.class);

        primitiveMap.put("float", float.class);
        primitiveMap.put("double", double.class);

        primitiveMap.put("boolean", boolean.class);

        primitiveMap.put("char", char.class);
    }

    public ColumnModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(final String dbName) {
        this.dbName = dbName;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public void setDbTableName(final String dbTableName) {
        this.dbTableName = dbTableName;
    }

    public String getDbFullTableName() {
        return dbFullTableName;
    }

    public void setDbFullTableName(final String dbFullTableName) {
        this.dbFullTableName = dbFullTableName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(final String sqlType) {
        this.sqlType = sqlType;
    }

    public String getJavaTypeName() {
        return javaTypeName;
    }

    public void setJavaTypeName(String javaTypeName) {
        this.javaTypeName = javaTypeName;
    }

    public boolean isPrimitiveType() {
        return ModelUtil.PRIMITIVE_TYPE_NAMES.contains(javaTypeName);
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(final boolean notNull) {
        this.notNull = notNull;
    }

    public int getSqlTypeInt() {
        return sqlTypeInt;
    }

    public void setSqlTypeInt(final int sqlTypeInt) {
        this.sqlTypeInt = sqlTypeInt;
    }

    public boolean isPartOfPrimaryKey() {
        return partOfPrimaryKey;
    }

    public void setPartOfPrimaryKey(final boolean partOfPrimaryKey) {
        this.partOfPrimaryKey = partOfPrimaryKey;
    }

    public boolean isPartOfForeignKey() {
        return partOfForeignKey;
    }

    public void setPartOfForeignKey(final boolean partOfForeignKey) {
        this.partOfForeignKey = partOfForeignKey;
    }

    public boolean isExplicitAttribute() {
        return explicitAttribute;
    }

    public void setExplicitAttribute(boolean explicitAttribute) {
        this.explicitAttribute = explicitAttribute;
    }

    public List<ConvenienceSetterModel> getConvenienceSetters() {
        return convenienceSetters;
    }

    public void setConvenienceSetters(List<ConvenienceSetterModel> convenienceSetters) {
        this.convenienceSetters = convenienceSetters;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(final boolean unique) {
        this.unique = unique;
    }

    public Class<?> getJavaTypeAsClass() {
        try {
            return primitiveMap.getOrDefault(this.javaTypeName, Class.forName(this.javaTypeName));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
