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


public class RedGVisualizationRelation {

    private String from;
    private String to;
    private String name;
    private String sqlName;

    public RedGVisualizationRelation(final String from, final String to, final String name) {
        this.from = from;
        this.to = to;
        this.name = name;
    }

    public RedGVisualizationRelation(final String from, final String to, final String name, final String sqlName) {
        this.from = from;
        this.to = to;
        this.name = name;
        this.sqlName = sqlName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(final String to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(final String sqlName) {
        this.sqlName = sqlName;
    }
}
