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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link PluggableDefaultValueProvider} that provides incrementing numbers. Each number start by 0 or the specified
 * number. Each column in each table has its own counter.
 */
public class IncrementingNumberProvider extends NumberProvider {

    private final Map<String, BigDecimal> values = new HashMap<>();

    private BigDecimal startValue;

    public IncrementingNumberProvider() {
        this(BigDecimal.ZERO);
    }

    public IncrementingNumberProvider(BigDecimal value) {
        this.startValue = value;
    }

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        final String key = columnModel.getDbFullTableName() + "." + columnModel.getDbName();
        BigDecimal number = values.getOrDefault(key, startValue);
        number = number.add(BigDecimal.ONE);
        values.put(key, number);
        return convertNumber(number, type);
    }
}
