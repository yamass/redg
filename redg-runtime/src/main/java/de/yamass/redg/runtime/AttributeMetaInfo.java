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

package de.yamass.redg.runtime;

public class AttributeMetaInfo {

    private final String dbColumnName;
    private final String dbTableName;
    private final String dbFullTableName;
    private final String sqlType;
    private final int sqlTypeInt;
    private final Class javaType;
    private final boolean notNull;

    public AttributeMetaInfo(
            String dbColumnName,
            String dbTableName,
            String dbFullTableName,
            String sqlType,
            int sqlTypeInt,
            Class javaType,
            boolean notNull
    ) {
        this.dbColumnName = dbColumnName;
        this.dbTableName = dbTableName;
        this.dbFullTableName = dbFullTableName;
        this.sqlType = sqlType;
        this.sqlTypeInt = sqlTypeInt;
        this.javaType = javaType;
        this.notNull = notNull;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public String getDbFullTableName() {
        return dbFullTableName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public int getSqlTypeInt() {
        return sqlTypeInt;
    }

    public Class getJavaType() {
        return javaType;
    }

    public boolean isNotNull() {
        return notNull;
    }

}
