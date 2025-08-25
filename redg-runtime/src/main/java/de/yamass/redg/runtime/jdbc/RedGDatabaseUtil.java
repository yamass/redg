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

package de.yamass.redg.runtime.jdbc;

import de.yamass.redg.runtime.AttributeMetaInfo;
import de.yamass.redg.runtime.ExistingEntryMissingException;
import de.yamass.redg.runtime.InsertionFailedException;
import de.yamass.redg.runtime.RedGEntity;
import de.yamass.redg.runtime.transformer.DefaultPreparedStatementParameterSetter;
import de.yamass.redg.runtime.transformer.PreparedStatementParameterSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedGDatabaseUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RedGDatabaseUtil.class);

    private RedGDatabaseUtil() {

    }

    /**
     * @param connection The JDBC connection
     * @param gObjects   The entities to insert
     * @see RedGDatabaseUtil#insertDataIntoDatabase(List, Connection, PreparedStatementParameterSetter)
     */
    public static void insertDataIntoDatabase(List<? extends RedGEntity> gObjects, final Connection connection) {
        insertDataIntoDatabase(gObjects, connection, new DefaultPreparedStatementParameterSetter());
    }

    /**
     * Inserts all data previously prepared by RedG into the database the {@link Connection} connects to.
     * Entities are going to be inserted in the order they were added. <br>
     * This method uses prepared statements to efficiently insert even great amount of data.<br>
     * If a Insertion/Update updates more than 1 entry, a warning will be logged to the console.
     * If a entity that is marked as "existing" (via redG.existingX()) is not found, an error will be logged and an {@link ExistingEntryMissingException} will
     * be thrown.
     *
     * @param connection                       The database connection
     * @param gObjects                         The entities that should be inserted into the database
     * @param preparedStatementParameterSetter The prepared statement parameter setter that should be used to set the values on the prepared statements
     * @throws ExistingEntryMissingException When an entry defined as "existing" (via redG.existingX()) cannot be found in the database
     * @throws InsertionFailedException      When problems with the prepared statement occur. This is often the result of a faulty data type mapping or
     *                                       {@link PreparedStatementParameterSetter}
     */
    public static void insertDataIntoDatabase(List<? extends RedGEntity> gObjects, final Connection connection,
                                              PreparedStatementParameterSetter preparedStatementParameterSetter) {
        final Map<Class<? extends RedGEntity>, PreparedStatement> statementMap = gObjects.stream()
                .filter(distinctByKey(RedGEntity::getClass))
                .collect(HashMap::new, (m, obj) -> m.put(obj.getClass(), prepareStatement(connection).apply(obj)), HashMap::putAll);
        for (final RedGEntity obj : gObjects) {
            final PreparedStatement statement = statementMap.get(obj.getClass());
            if (statement == null) {
                throw new InsertionFailedException("Could not get prepared statement for class " + obj.getClass().getName());
            }
            final Object[] values = obj.getPreparedStatementValues();
            for (int i = 0; i < values.length; i++) {
                try {
                    AttributeMetaInfo[] preparedStatementValueMetaInfo = obj.getPreparedStatementValuesMetaInfos();
                    if (values[i] == null) {
                        statement.setNull(i + 1, preparedStatementValueMetaInfo[i].getSqlTypeInt());
                    } else {
                        preparedStatementParameterSetter.setParameter(statement,
                                i + 1, values[i], preparedStatementValueMetaInfo[i], connection);
                    }
                } catch (SQLException e) {
                    throw new InsertionFailedException("Setting value for statement failed", e);
                }

            }
            try {
                final boolean resultType = statement.execute();
                if (resultType) {
                    // resultType == true means that a ResultSet was returned that can be obtained by calling getResultSet()
                    // as an INSERT does not return a ResultSet, this means that this statement was used to check if a entry specified as already existing
                    // does really exist.
                    final ResultSet rs = statement.getResultSet();
                    rs.next();
                    if (rs.getInt(1) != 1) {
                        LOG.error(
                                "The entry of type {} was specified as existing (PKs: {}) but could not be found/identified in the database."
                                        + " The test query found {} matches. "
                                        + " If you modelled the searched entity via RedG, you should call findSingleEntity() instead.",
                                obj.getClass(), obj.getPreparedStatementValues(), rs.getInt(1));
                        throw new ExistingEntryMissingException("The entry of type " + obj.getClass()
                                + ", identified by " + Arrays.toString(obj.getPreparedStatementValues()) + " was not found!");
                    }

                } else {
                    // resultType == false means that no ResultSet was returned. Thus the executed statement was a regular insert.
                    if (statement.getUpdateCount() != 1) {
                        LOG.warn("Insert statement updated more that one database entry. {} entries were updated", statement.getUpdateCount());
                    }
                }
                statement.clearParameters();
                //LOG.debug("Executed statement");
            } catch (SQLException e) {
                throw new InsertionFailedException("SQL execution failed", e);
            }
        }
    }

    private static Function<RedGEntity, PreparedStatement> prepareStatement(final Connection connection) {
        return (gis) -> {
            try {
                return connection.prepareStatement(gis.getPreparedStatementString());
            } catch (SQLException e) {
                LOG.error("Could not create prepared statement", e);
                return null;
            }
        };
    }

    /**
     * Taken from http://stackoverflow.com/a/27872852
     *
     * @param keyExtractor The key extractor function
     * @param <T>          the generic type
     * @return a predicate for filtering
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
