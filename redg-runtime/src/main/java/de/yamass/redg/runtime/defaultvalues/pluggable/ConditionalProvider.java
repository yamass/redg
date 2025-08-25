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

package de.yamass.redg.runtime.defaultvalues.pluggable;

import de.yamass.redg.models.ColumnModel;

import java.util.regex.Pattern;

/**
 * A {@link PluggableDefaultValueProvider} that delegates the value request to a provided {@link PluggableDefaultValueProvider} if the table and column match
 * a specified regular expression.
 */
public class ConditionalProvider implements PluggableDefaultValueProvider {

    private final PluggableDefaultValueProvider internalProvider;
    private final Pattern fullTablePattern, tablePattern, columnPattern;

    public ConditionalProvider(final PluggableDefaultValueProvider internalProvider, final String fullTableRegex,
                               final String tableRegex, final String columnRegex) {
        this.internalProvider = internalProvider;

        this.fullTablePattern = (fullTableRegex != null) ? Pattern.compile(fullTableRegex) : null;
        this.tablePattern = (tableRegex != null) ? Pattern.compile(tableRegex) : null;
        this.columnPattern = (columnRegex != null) ? Pattern.compile(columnRegex) : null;

    }

    @Override
    public boolean willProvide(final ColumnModel columnModel) {
        if (this.fullTablePattern != null && !this.fullTablePattern.matcher(columnModel.getDbFullTableName()).matches()) {
            return false;
        } else if (this.tablePattern != null && !this.tablePattern.matcher(columnModel.getDbTableName()).matches()) {
            return false;
        } else if (this.columnPattern != null && !this.columnPattern.matcher(columnModel.getDbName()).matches()) {
            return false;
        } else {
            return internalProvider.willProvide(columnModel);
        }
    }

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        if (willProvide(columnModel)) {
            return internalProvider.getDefaultValue(columnModel, type);
        } else {
            return null;
        }
    }
}
