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

package com.btc.redg.plugin;


import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import org.hibernate.cfg.Environment;
import org.junit.Rule;
import org.junit.Test;
import org.postgresql.Driver;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;


public class RedGGeneratorMojoTest {

    @Rule
    public final TestResources resources = new TestResources();

    @Rule
    public final TestMavenRuntime maven = new TestMavenRuntime();

    @Test
    public void test() throws Exception {
        File baseDir = resources.getBasedir("full-project-test");
        maven.executeMojo(baseDir, "redg", TestHelpers.getArrayParameters("sqlScripts", "test.sql"));
        // TODO: actually test something
    }

    @Test
    public void testLiquibase() throws Exception {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>();
        postgres.start();
        final String url = postgres.getJdbcUrl();
        final String user = postgres.getUsername();
        final String pass = postgres.getPassword();
        File baseDir = resources.getBasedir("liquibase-project-test");
        maven.executeMojo(baseDir, "redg",
            TestHelpers.getParameters("liquibaseChangeLogFile", "src/test/projects/liquibase-project-test/src/main/resources/changelog.xml"),
            TestHelpers.getParameters("username", user),
            TestHelpers.getParameters("password", pass),
            TestHelpers.getParameters("connectionString", url),
            TestHelpers.getParameters("jdbcDriver", Driver.class.getCanonicalName()));
        // TODO: actually test something
    }
}
