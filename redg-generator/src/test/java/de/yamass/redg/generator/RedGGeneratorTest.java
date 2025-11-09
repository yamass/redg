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

package de.yamass.redg.generator;

import de.yamass.redg.generator.exceptions.RedGGenerationException;
import de.yamass.redg.util.ScriptRunner;
import org.assertj.core.api.Assertions;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.IncludeAll;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


class RedGGeneratorTest {

    @Test
    void testGenerateCode_Working() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-test-all", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        org.junit.jupiter.api.Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        Path p = Files.createTempDirectory("redg-test");
        RedGGenerator.generateCode(dataSource,
                new IncludeAll(),
                new IncludeAll(),
                null,
                "",
                p,
                null,
                null,
                null,
                null,
                false);

        Path mainFile = Paths.get(p.toAbsolutePath().toString(), "de", "yamass", "redg", "generated", "RedG.java");
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(mainFile));

        Path classFileUser = Paths.get(p.toAbsolutePath().toString(), "de", "yamass", "redg", "generated", "GDemoUser.java");
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(mainFile));

        Path classFileCompany = Paths.get(p.toAbsolutePath().toString(), "de", "yamass", "redg", "generated", "GDemoCompany.java");
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(mainFile));


    }

    @Test
    void testGenerateCode_NoTables() throws Exception {

        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-test-all2", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        org.junit.jupiter.api.Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        Path p = Files.createTempDirectory("redg-test");

        Assertions.assertThatThrownBy(() -> {
                    RedGGenerator.generateCode(dataSource,
                            new ExcludeAll(),
                            new ExcludeAll(),
                            null,
                            "",
                            p,
                            null,
                            null,
                            null,
                            null,
                            false);
                }).isInstanceOf(RedGGenerationException.class)
                .hasMessage("Crawling failed");

    }

    @Test
    void testGenerateCode_CannotWriteFolder() throws Exception {

        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-test-all3", "", "");
        File tempFile = Helpers.getResourceAsFile("codegenerator/test.sql");
        org.junit.jupiter.api.Assertions.assertNotNull(tempFile);
        ScriptRunner.executeScripts(dataSource, new File[]{tempFile});

        Path p;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Simply a path that exists but writing should not be possible for this process. If it is, maven is running as an administrator / system user.
            // This would not be good.
            p = Paths.get("C:/windows/system32/redg");
        } else {
            // This should exist on basically every unix system and the process should not have permission to write there
            // If this process can write there because it is run with root privileges or the user has write access, they have a much bigger problem than a
            // failing unit test
            p = Paths.get("/dev/redg");
        }

        Assertions.assertThatThrownBy(() -> {
            RedGGenerator.generateCode(dataSource,
                    new IncludeAll(),
                    new IncludeAll(),
                    null,
                    "",
                    p,
                    null,
                    null,
                    null,
                    null,
                    false);
        }) .isInstanceOf(RedGGenerationException.class)
                .hasMessage("Creation of package folder structure failed");
    }
}
