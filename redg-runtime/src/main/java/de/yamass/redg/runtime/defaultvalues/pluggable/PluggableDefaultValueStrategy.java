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
import de.yamass.redg.runtime.defaultvalues.DefaultValueStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link DefaultValueStrategy} that implements a simple plugin system. Add your {@link PluggableDefaultValueProvider}s to it in
 * the order they should be used.
 * <p>
 * Note: if your provider returns {@code true} for {@link PluggableDefaultValueProvider#willProvide(ColumnModel)}, its
 * value will be queried, even if it might not be used. A provider providing a not-null value will always be preferred.
 */
public class PluggableDefaultValueStrategy implements DefaultValueStrategy {

    private List<PluggableDefaultValueProvider> providers = new ArrayList<>();

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        return providers.stream()
                .filter(provider -> provider.willProvide(columnModel))
                .map(provider -> provider.getDefaultValue(columnModel, type))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public List<PluggableDefaultValueProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<PluggableDefaultValueProvider> providers) {
        this.providers = providers;
    }

    /**
     * Adds a new provider to the end of the list of providers.
     *
     * @param provider The provider to add
     */
    public void addProvider(final PluggableDefaultValueProvider provider) {
        this.providers.add(provider);
    }

}
