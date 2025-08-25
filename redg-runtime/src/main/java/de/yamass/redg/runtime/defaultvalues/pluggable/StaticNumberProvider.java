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

/**
 * A {@link PluggableDefaultValueProvider} that provides a static number. The list of supported data types can be found
 * at the {@link NumberProvider}
 *
 * @see NumberProvider
 */
public class StaticNumberProvider extends NumberProvider {

    private final BigDecimal number;

    public StaticNumberProvider(long value) {
        this.number = BigDecimal.valueOf(value);
    }

    public StaticNumberProvider(BigDecimal value) {
        this.number = value;
    }

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        return convertNumber(number, type);
    }
}
