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

package de.yamass.redg.generator.extractor;

import de.yamass.redg.generator.Helpers;
import de.yamass.redg.generator.testutil.DatabaseTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DatabaseManagerTest {

    @Test
    void testExecuteScripts_NoScripts() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:redg", "", "");
        Assertions.assertNotNull(dataSource);

        DatabaseManager.executeScripts(dataSource, null);
        DatabaseManager.executeScripts(dataSource, new File[0]);
    }

    @Test
    void testExecuteScripts_ScriptsInvalidSQL() throws Exception {
        DataSource dataSource = DatabaseTestUtil.createH2DataSource("jdbc:h2:mem:redg", "", "");
        Assertions.assertNotNull(dataSource);
        assertThatThrownBy(() -> DatabaseManager.executeScripts(dataSource, new File[]{Helpers.getResourceAsFile("invalid.sql")}))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("CREATE TABLE NOPENOPENOPE");
    }

}
