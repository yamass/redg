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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.yamass.redg.runtime.defaultvalues.DefaultDefaultValueStrategy;
import de.yamass.redg.runtime.defaultvalues.DefaultValueStrategy;
import de.yamass.redg.runtime.dummy.DefaultDummyFactory;
import de.yamass.redg.runtime.dummy.DummyFactory;
import de.yamass.redg.runtime.insertvalues.DefaultSQLValuesFormatter;
import de.yamass.redg.runtime.insertvalues.SQLValuesFormatter;
import de.yamass.redg.runtime.jdbc.RedGDatabaseUtil;
import de.yamass.redg.runtime.transformer.DefaultPreparedStatementParameterSetter;
import de.yamass.redg.runtime.transformer.PreparedStatementParameterSetter;

/**
 * The abstract super class for all RedG main classes.
 */
public abstract class AbstractRedG {

    private DefaultValueStrategy defaultValueStrategy = new DefaultDefaultValueStrategy();

    private SQLValuesFormatter sqlValuesFormatter = new DefaultSQLValuesFormatter();

    private PreparedStatementParameterSetter preparedStatementParameterSetter = new DefaultPreparedStatementParameterSetter();

    private DummyFactory dummyFactory = new DefaultDummyFactory();

    private List<RedGEntity> entities = new LinkedList<>();

    public void addEntity(final RedGEntity entity) {
        this.entities.add(entity);
    }

    /**
     * Returns the default value strategy used to fill not provided values for the entities
     *
     * @return The current default value strategy
     */
    public DefaultValueStrategy getDefaultValueStrategy() {
        return defaultValueStrategy;
    }

    /**
     * Sets the default value strategy. May only be called before entities are created, otherwise an {@link IllegalStateException} gets thrown.
     * Passing {@code null} resets the strategy to a {@link DefaultDefaultValueStrategy}.
     *
     * @param defaultValueStrategy The new default value strategy to use from now on
     */
    public void setDefaultValueStrategy(final DefaultValueStrategy defaultValueStrategy) {
        if (entities.size() > 0) {
            throw new IllegalStateException("The default value strategy cannot be changed after an entity was generated!");
        }
        if (defaultValueStrategy == null) {
            this.defaultValueStrategy = new DefaultDefaultValueStrategy();
        } else {
            this.defaultValueStrategy = defaultValueStrategy;
        }
    }

    /**
     * Returns the formatter used to format the values for the sql insert string.
     *
     * @return The current value formatter
     */
    public SQLValuesFormatter getSqlValuesFormatter() {
        return sqlValuesFormatter;
    }

    /**
     * Sets the insert value formatter. Passing {@code null} resets it to a {@link DefaultSQLValuesFormatter}.
     * Must not be called after entities are created or an {@link IllegalStateException} gets thrown.
     *
     * @param sqlValuesFormatter The new insert value formatter
     */
    public void setSqlValuesFormatter(final SQLValuesFormatter sqlValuesFormatter) {
        if (entities.size() > 0) {
            throw new IllegalStateException("The SQL values formatter cannot be changed after an entity was generated!");
        }
        if (sqlValuesFormatter == null) {
            this.sqlValuesFormatter = new DefaultSQLValuesFormatter();
        } else {
            this.sqlValuesFormatter = sqlValuesFormatter;
        }
    }

    /**
     * Returns the transformer that is used to set parameter values on {@link PreparedStatement}s.
     *
     * @return The PreparedStatementParameterSetter
     */
    public PreparedStatementParameterSetter getPreparedStatementParameterSetter() {
        return preparedStatementParameterSetter;
    }

    /**
     * Sets the PreparedStatementParameterSetter that will be used to set parameter values on {@link PreparedStatement}s.
     * Must not be called after entities are created or an {@link IllegalStateException} gets thrown.
     *
     * @param preparedStatementParameterSetter new The PreparedStatementParameterSetter.
     */
    public void setPreparedStatementParameterSetter(final PreparedStatementParameterSetter preparedStatementParameterSetter) {
        if (entities.size() > 0) {
            throw new IllegalStateException("The PreparedStatement parameter setter cannot be changed after an entity was generated!");
        }
        if (preparedStatementParameterSetter == null) {
            this.preparedStatementParameterSetter = new DefaultPreparedStatementParameterSetter();
        } else {
            this.preparedStatementParameterSetter = preparedStatementParameterSetter;
        }
    }

    /**
     * Returns the current {@link DummyFactory} that will be used to create dummy entities.
     *
     * @return The current dummy factory
     */
    public DummyFactory getDummyFactory() {
        return dummyFactory;
    }

    /**
     * Sets the new dummy factory, that will be used to generate dummy entities from now on. Changing this does not affect old dummy entities.
     * Must not be called after entities are created or an {@link IllegalStateException} gets thrown.
     *
     * @param dummyFactory The new dummy factory. If {@code null}, a {@link DefaultDummyFactory} gets used.
     */
    public void setDummyFactory(final DummyFactory dummyFactory) {
        if (entities.size() > 0) {
            throw new IllegalStateException("The dummy factory cannot be changed after an entity was generated!");
        }
        if (dummyFactory == null) {
            this.dummyFactory = new DefaultDummyFactory();
        } else {
            this.dummyFactory = dummyFactory;
        }
    }

    /**
     * Returns a list of insert statements, one for each added entity in the respective order they were added.
     *
     * @return The SQL Insert strings
     */
    public List<String> generateSQLStatements() {
        return getEntitiesSortedForInsert().stream()
                .map(RedGEntity::getSQLString)
                .collect(Collectors.toList());
    }

    /**
     * Inserts all data previously prepared by RedG into the database the {@link Connection} connects to.
     * Entities are going to be inserted in the order they were added. <br>
     * This method uses prepared statements to efficiently insert even great amount of data.<br>
     * If a Insertion/Update updates more than 1 entry, a warning will be logged to the console.
     * If a entity that is marked as "existing" (via redG.existingX()) is not found, an error will be logged and an {@link ExistingEntryMissingException} will
     * be thrown.
     *
     * @param connection The database connection
     * @throws ExistingEntryMissingException When an entry defined as "existing" (via redG.existingX()) cannot be found in the database
     * @throws InsertionFailedException      When problems with the prepared statement occur. This is often the result of a faulty data type mapping or
     *                                       {@link PreparedStatementParameterSetter}
     */
    public void insertDataIntoDatabase(final Connection connection) {
        RedGDatabaseUtil.insertDataIntoDatabase(getEntitiesSortedForInsert(), connection, preparedStatementParameterSetter);
    }

    /**
     * Finds a single entity in the list of entities to insert into the database. If multiple entities match the {@link Predicate}, the entity that was added
     * first will be returned.
     *
     * @param type   The class of the entity
     * @param filter A predicate that gets called for every entity that has the requested type. Should return {@code true} only for the entity that should be
     *               found
     * @param <T>    The entity type
     * @return The found entity. If no entity is found an {@link IllegalArgumentException} gets thrown.
     */
    public <T extends RedGEntity> T findSingleEntity(final Class<T> type, final Predicate<T> filter) {
        return this.entities.stream()
                .filter(obj -> Objects.equals(type, obj.getClass()))
                .map(type::cast)
                .filter(filter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find an entity that satisfies the filter!"));
    }

    /**
     * Searches through the list of entities to insert into the database and returns all of the specified type that match the passed {@link Predicate}.
     *
     * @param type   The class of the entities that should be searched for
     * @param filter A predicate that gets called for every entity that has the requested type. An entity gets added to the entities returned by this method if
     *               the predicate returns {@code true}
     * @param <T>    The entity type
     * @return A list of all entities of the requested type that match the predicate
     */
    public <T extends RedGEntity> List<T> findEntities(final Class<T> type, final Predicate<T> filter) {
        return this.entities.stream()
                .filter(obj -> Objects.equals(type, obj.getClass()))
                .map(type::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<RedGEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public List<RedGEntity> getEntitiesSortedForInsert() {
        return EntitySorter.sortEntities(entities);
    }

    public abstract String getVisualizationJson();


}
