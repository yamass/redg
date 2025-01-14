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

import com.btc.redg.generator.Helpers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

import java.io.File;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class DatabaseManagerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConnectToDatabase_H2Success() throws Exception {
        DatabaseConnectionSource databaseConnectionSource = DatabaseManager.createConnectionSource("jdbc:h2:mem:redg", "", "");
        assertNotNull(databaseConnectionSource);
//        assertTrue(connection.isValid(10));
    }

    @Ignore //TODO
    @Test
    public void testConnectToDatabase_InvalidDriver() throws Exception {
        thrown.expect(ClassNotFoundException.class);
        DatabaseManager.createConnectionSource("jdbc:dne:mem:redg", "", "");
    }

    @Ignore //TODO
    @Test
    public void testConnectToDatabase_FailedConnection() throws Exception {
        thrown.expect(SQLException.class);
        DatabaseManager.createConnectionSource("jdbc:h4:invalid:redg", "", "");
    }

    @Test
    public void testExecutePreparationScripts_NoScripts() throws Exception {
        DatabaseConnectionSource databaseConnectionSource = DatabaseManager.createConnectionSource("jdbc:h2:mem:redg", "", "");
        assertNotNull(databaseConnectionSource);

        DatabaseManager.executePreparationScripts(databaseConnectionSource, null);
        DatabaseManager.executePreparationScripts(databaseConnectionSource, new File[0]);
    }

    @Test
    public void testExecutePreparationScripts_ScriptsInvalidSQL() throws Exception {
        thrown.expect(SQLException.class);
        thrown.expectMessage("CREATE TABLE NOPENOPENOPE");
        DatabaseConnectionSource databaseConnectionSource = DatabaseManager.createConnectionSource("jdbc:h2:mem:redg", "", "");
        assertNotNull(databaseConnectionSource);

        DatabaseManager.executePreparationScripts(databaseConnectionSource, new File[]{Helpers.getResourceAsFile("invalid.sql")});
    }

}
