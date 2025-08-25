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

/**
 * A {@link PluggableDefaultValueProvider} that provides a constant object for every field with the exact type. Pair with {@link ConditionalProvider} to limit
 * the tables/columns this provider will provide values for.
 */
public class ConstantValueProvider implements PluggableDefaultValueProvider {

    private final Object value;

    public ConstantValueProvider(final Object value) {
        this.value = value;
    }

    @Override
    public boolean willProvide(final ColumnModel columnModel) {
        return value.getClass().equals(columnModel.getJavaTypeAsClass());
    }

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        return type.cast(value);
    }
}

