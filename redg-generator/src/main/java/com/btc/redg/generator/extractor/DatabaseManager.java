/*
 * Copyright 2017 BTC Business Technology AG
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

package com.btc.redg.generator.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btc.redg.generator.extractor.utils.NullPrintWriter;
import com.btc.redg.generator.extractor.utils.ScriptRunner;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.RoutineType;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schemacrawler.*;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.tools.utility.SchemaCrawlerUtility;
import us.fatehi.utility.datasource.DatabaseConnectionSource;
import us.fatehi.utility.datasource.DatabaseConnectionSources;
import us.fatehi.utility.datasource.MultiUseUserCredentials;

/**
 * <p>
 * A class that provided useful methods for connecting to a extractor, executing SQL scripts and analyzing the extractor.
 * </p><p>
 * The normal workflow would be to connect to the extractor, execute a few scripts and then run the analysis.
 * </p>
 * <p><blockquote><pre>
 * Connection conn = DatabaseManager.connectToDatabase(...);
 * DatabaseManager.executePreparationScripts(conn, scripts);
 * Database db = DatabaseManager.crawlDatabase(conn, schemaRule, tableRule);
 * </pre></blockquote>
 */
public class DatabaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseManager.class);


    public static DatabaseConnectionSource createConnectionSource(final String connectionString,
                                                                  final String username,
                                                                  final String password) {
        return DatabaseConnectionSources.newDatabaseConnectionSource(connectionString, new MultiUseUserCredentials(username, password));
    }

    /**
     * Executes the SQL scripts specified in the {@code sqlScripts} parameter.
     * The SQL gets split into statements and gets executed via the {@code connection} by a {@link ScriptRunner}. The output from the script runner simply gets
     * discarded.
     *
     * @param databaseConnectionSource provides the JDBC connection to use for script execution
     * @param sqlScripts               The array containing all sql files that should be executed
     * @throws IOException  Gets thrown when a file IO fails
     * @throws SQLException Gets thrown when the a part of the SQL could not be executed
     */
    public static void executePreparationScripts(final DatabaseConnectionSource databaseConnectionSource, final File[] sqlScripts) throws IOException, SQLException {
        LOG.info("Executing provided SQL scripts...");
        if (sqlScripts == null || sqlScripts.length == 0) {
            LOG.info("No SQL script was provided.");
            return;
        }
        final ScriptRunner scriptRunner = new ScriptRunner(databaseConnectionSource.get(), true, true);
        scriptRunner.setErrorLogWriter(new NullPrintWriter());
        scriptRunner.setLogWriter(new NullPrintWriter());
        for (final File sqlScript : sqlScripts) {
            LOG.info("Processing script " + sqlScript.getAbsolutePath());
            try {
                scriptRunner.runScript(new BufferedReader(new FileReader(sqlScript)));
                LOG.info("Script finished successfully.");
            } catch (IOException e) {
                if (e.getCause() instanceof SQLException) {
                    LOG.error("SQL execution failed", e);
                    throw (SQLException) e.getCause();
                }
                LOG.error("Some IO failed while trying to run the script", e);
                throw e;
            } catch (SQLException e) {
                LOG.error("SQL execution failed", e);
                throw e;
            }
        }
    }

    /**
     * Starts the schema crawler and lets it crawl the given JDBC connection.
     *
     * @param databaseConnectionSource provides the JDBC connection
     * @param schemaRule               The {@link InclusionRule} to be passed to SchemaCrawler that specifies which schemas should be analyzed
     * @param tableRule                The {@link InclusionRule} to be passed to SchemaCrawler that specifies which tables should be analyzed. If a table is included by the
     *                                 {@code tableRule} but excluded by the {@code schemaRule} it will not be analyzed.
     * @return The populated {@link Catalog} object containing the metadata for the extractor
     * @throws SchemaCrawlerException Gets thrown when the database could not be crawled successfully
     */
    public static Catalog crawlDatabase(final DatabaseConnectionSource databaseConnectionSource, final InclusionRule schemaRule, final InclusionRule tableRule) throws SchemaCrawlerException {
        final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                .withLoadOptions(LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.builder()
                        .withTag("standard")
                        .withInfoLevel(InfoLevel.standard)
                        .setRetrieveIndexes(false)
                        .toOptions()
                ).toOptions())
                .withLimitOptions(LimitOptionsBuilder.builder()
                        .routineTypes(Arrays.asList(RoutineType.procedure, RoutineType.unknown))
                        .includeSchemas(schemaRule == null ? new IncludeAll() : schemaRule)
                        .includeTables(tableRule == null ? new IncludeAll() : tableRule)
                        .toOptions());
        //TODO check equality between new and old
//                .withSchemaInfoLevel(SchemaInfoLevelBuilder.standard().setRetrieveIndexes(false))
//                .routineTypes(Arrays.asList(RoutineType.procedure, RoutineType.unknown))
//                .includeSchemas(schemaRule == null ? new IncludeAll() : schemaRule)
//                .includeTables(tableRule == null ? new IncludeAll() : tableRule)
//                .toOptions();

        try {
            return SchemaCrawlerUtility.getCatalog(databaseConnectionSource, options);
        } catch (SchemaCrawlerException e) {
            LOG.error("Schema crawling failed with exception", e);
            throw e;
        }
    }

}
