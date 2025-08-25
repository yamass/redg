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

/**
 * Simple wrapper for the extractor connection information
 */
public class DatabaseConnectionParameter {

    private String jdbcDriver;

    private String connectionString;

    private String username;

    private String password;

    public DatabaseConnectionParameter(final String jdbcDriver, final String connectionString, final String username, final String password) {
        this.jdbcDriver = jdbcDriver;
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(final String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(final String connectionString) {
        this.connectionString = connectionString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
