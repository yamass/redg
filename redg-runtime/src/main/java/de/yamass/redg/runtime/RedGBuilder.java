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

import de.yamass.redg.runtime.defaultvalues.DefaultValueStrategy;
import de.yamass.redg.runtime.dummy.DummyFactory;
import de.yamass.redg.runtime.insertvalues.SQLValuesFormatter;
import de.yamass.redg.runtime.transformer.PreparedStatementParameterSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple builder that allows you to easily create a customized RedG instance.
 */
public class RedGBuilder<T extends AbstractRedG> {

    private static Logger LOG = LoggerFactory.getLogger(RedGBuilder.class);

    private T instance;

    /**
     * Creates a new builder that attempts to load the default RedG class (de.yamass.redg.generated.RedG) and use it as the instance.
     * If you changed the package or class name, use {@link RedGBuilder#RedGBuilder(Class)} instead.
     */
    @SuppressWarnings("unchecked")
    public RedGBuilder() {
        try {
            Class<T> clazz = (Class<T>) Class.forName("de.yamass.redg.generated.RedG");
            instance = clazz.newInstance();
        } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
            LOG.error("Could not load default RedG class", e);
            throw new RuntimeException("Could not load default RedG class", e);
        }
    }

    /**
     * Creates a new builder
     *
     * @param clazz The RedG main class to be used
     */
    public RedGBuilder(final Class<T> clazz) {
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Could not instantiate RedG instance", e);
            throw new RuntimeException("Could not instantiate RedG instance", e);
        }
    }

    /**
     * Sets the default value strategy
     * @param strategy The default value strategy
     * @return The builder itself
     * @see AbstractRedG#setDefaultValueStrategy(DefaultValueStrategy)
     */
    public RedGBuilder<T> withDefaultValueStrategy(final DefaultValueStrategy strategy) {
        if (instance == null) {
            throw new IllegalStateException("Using the builder after build() was called is not allowed!");
        }
        instance.setDefaultValueStrategy(strategy);
        return this;
    }

    /**
     * Sets the PreparedStatement parameter setter
     * @param setter The PreparedStatement parameter setter
     * @return The builder itself
     * @see AbstractRedG#setPreparedStatementParameterSetter(PreparedStatementParameterSetter)
     */
    public RedGBuilder<T> withPreparedStatementParameterSetter(final PreparedStatementParameterSetter setter) {
        if (instance == null) {
            throw new IllegalStateException("Using the builder after build() was called is not allowed!");
        }
        instance.setPreparedStatementParameterSetter(setter);
        return this;
    }

    /**
     * Sets the SQL values formatter
     * @param formatter The SQL values formatter
     * @return The builder itself
     * @see AbstractRedG#setSqlValuesFormatter(SQLValuesFormatter)
     */
    public RedGBuilder<T> withSqlValuesFormatter(final SQLValuesFormatter formatter) {
        if (instance == null) {
            throw new IllegalStateException("Using the builder after build() was called is not allowed!");
        }
        instance.setSqlValuesFormatter(formatter);
        return this;
    }

    /**
     * Sets the dummy factory
     * @param dummyFactory The dummy factory
     * @return The builder itself
     * @see AbstractRedG#setDummyFactory(DummyFactory)
     */
    public RedGBuilder<T> withDummyFactory(final DummyFactory dummyFactory) {
        if (instance == null) {
            throw new IllegalStateException("Using the builder after build() was called is not allowed!");
        }
        instance.setDummyFactory(dummyFactory);
        return this;
    }

    /**
     * Returns the customized RedG main class
     * @return The RedG main class
     */
    public T build() {
        final T inst = instance;
        instance = null;
        return inst;
    }
}
