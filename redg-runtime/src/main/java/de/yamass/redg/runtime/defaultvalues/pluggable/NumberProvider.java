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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link PluggableDefaultValueProvider} that is capable of providing numbers.
 * The supported number types are
 * <ul>
 * <li>BigDecimal</li>
 * <li>Double</li>
 * <li>Float</li>
 * <li>Long</li>
 * <li>Integer</li>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>AtomicInteger</li>
 * <li>AtomicLong</li>
 * </ul>
 */
public abstract class NumberProvider implements PluggableDefaultValueProvider {

    @Override
    public boolean willProvide(final ColumnModel columnModel) {
        return Number.class.isAssignableFrom(columnModel.getJavaTypeAsClass());
    }

    public static <T> T convertNumber(BigDecimal number, Class<T> type) {
        if (type == double.class) {
            type = (Class<T>) Double.class;
        } else if (type == float.class) {
            type = (Class<T>) Float.class;
        } else if (type == long.class) {
            type = (Class<T>) Long.class;
        } else if (type == int.class) {
            type = (Class<T>) Integer.class;
        } else if (type == byte.class) {
            type = (Class<T>) Byte.class;
        } else if (type == short.class) {
            type = (Class<T>) Short.class;
        }

        if (BigDecimal.class.equals(type)) {
            return type.cast(number);
        } else if (Double.class.equals(type)) {
            return type.cast(number.doubleValue());
        } else if (Float.class.equals(type)) {
            return type.cast(number.floatValue());
        } else if (Long.class.equals(type)) {
            return type.cast(number.longValueExact());
        } else if (Integer.class.equals(type)) {
            return type.cast(number.intValueExact());
        } else if (Byte.class.equals(type)) {
            return type.cast(number.byteValueExact());
        } else if (Short.class.equals(type)) {
            return type.cast(number.shortValueExact());
        } else if (AtomicInteger.class.equals(type)) {
            return type.cast(new AtomicInteger(number.intValueExact()));
        } else if (AtomicLong.class.equals(type)) {
            return type.cast(new AtomicLong(number.longValueExact()));
        }
        return null;
    }
}
