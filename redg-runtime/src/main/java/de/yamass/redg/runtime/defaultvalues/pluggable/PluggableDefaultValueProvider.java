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

/**
 * The interface to be used with the {@link PluggableDefaultValueStrategy}. This is basically a conditional {@link DefaultValueStrategy} that
 * will only be queried if {@link PluggableDefaultValueProvider#willProvide(ColumnModel)} returns true and
 * it is the first provider in the list of providers that returned true.
 */
public interface PluggableDefaultValueProvider extends DefaultValueStrategy {

    /**
     * Use this to perform your checks whether the class can and should provide a default value. Only return {@code true} if the class really can
     * provide a valid default value.
     *
     * @param columnModel the column model of the column that needs a default value
     * @return {@code true} if the class can and should provide a valid default value, {@code false} otherwise
     */
    boolean willProvide(ColumnModel columnModel);

}
