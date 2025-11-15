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

package de.yamass.redg.generator.utils;

import de.yamass.redg.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility class to detect the database type from a DataSource.
 */
public class DatabaseTypeDetector {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseTypeDetector.class);

    /**
     * Detects the database type from the given DataSource.
     *
     * @param dataSource The data source to detect the database type from
     * @return The detected database type, or GENERIC if detection fails
     */
    public static DatabaseType detectDatabaseType(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String databaseProductName = metadata.getDatabaseProductName();
            String driverName = metadata.getDriverName();

            if (databaseProductName != null) {
                String productNameLower = databaseProductName.toLowerCase();
                if (productNameLower.contains("postgresql") || productNameLower.contains("postgres")) {
                    LOG.debug("Detected PostgreSQL database");
                    return DatabaseType.POSTGRES;
                } else if (productNameLower.contains("mariadb")) {
                    LOG.debug("Detected MariaDB database");
                    return DatabaseType.MARIADB;
                } else if (productNameLower.contains("mysql")) {
                    // MySQL and MariaDB can be confused, but MariaDB usually identifies itself
                    LOG.debug("Detected MySQL database (falling back to MARIADB)");
                    return DatabaseType.MARIADB;
                } else if (productNameLower.contains("h2")) {
                    LOG.debug("Detected H2 database");
                    return DatabaseType.H2;
                }
            }

            // Fallback to driver name
            if (driverName != null) {
                String driverNameLower = driverName.toLowerCase();
                if (driverNameLower.contains("postgresql")) {
                    LOG.debug("Detected PostgreSQL from driver name");
                    return DatabaseType.POSTGRES;
                } else if (driverNameLower.contains("mariadb")) {
                    LOG.debug("Detected MariaDB from driver name");
                    return DatabaseType.MARIADB;
                } else if (driverNameLower.contains("mysql")) {
                    LOG.debug("Detected MySQL from driver name (falling back to MARIADB)");
                    return DatabaseType.MARIADB;
                } else if (driverNameLower.contains("h2")) {
                    LOG.debug("Detected H2 from driver name");
                    return DatabaseType.H2;
                }
            }

            LOG.warn("Could not detect database type from product name '{}' and driver '{}', using GENERIC", 
                    databaseProductName, driverName);
            return DatabaseType.GENERIC;
        } catch (SQLException e) {
            LOG.error("Failed to detect database type", e);
            return DatabaseType.GENERIC;
        }
    }
}

