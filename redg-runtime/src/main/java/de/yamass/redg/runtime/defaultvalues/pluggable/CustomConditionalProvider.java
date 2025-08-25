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

import java.util.function.Predicate;

/**
 * A conditional provider that is even more flexible than the {@link ConditionalProvider}
 */
public class CustomConditionalProvider implements PluggableDefaultValueProvider {

    private final Predicate<ColumnModel> provideCondition;
    private final PluggableDefaultValueProvider provider;

    public CustomConditionalProvider(final Predicate<ColumnModel> provideCondition, final PluggableDefaultValueProvider provider) {
        this.provideCondition = provideCondition;
        this.provider = provider;
    }

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        return provideCondition.test(columnModel) ?
                provider.getDefaultValue(columnModel, type) : null;
    }

    @Override
    public boolean willProvide(final ColumnModel columnModel) {
        return provideCondition.test(columnModel)
                && provider.willProvide(columnModel);
    }

}
