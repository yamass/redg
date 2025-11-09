package de.yamass.redg.util;

import de.yamass.redg.testing.Helpers;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScriptRunnerTest {

	@Test
	void testExecuteScripts_NoScripts() throws Exception {
		DataSource dataSource = createH2DataSource("jdbc:h2:mem:redg", "", "");
		Assertions.assertNotNull(dataSource);

		ScriptRunner.executeScripts(dataSource, null);
		ScriptRunner.executeScripts(dataSource, new File[0]);
	}

	@Test
	void testExecuteScripts_ScriptsInvalidSQL() throws Exception {
		DataSource dataSource = createH2DataSource("jdbc:h2:mem:redg", "", "");
		Assertions.assertNotNull(dataSource);
		assertThatThrownBy(() -> {
			File resourceAsFile = Helpers.getResourceAsFile("de/yamass/redg/util/invalid.sql");
			ScriptRunner.executeScripts(dataSource, new File[]{resourceAsFile});
		})
				.isInstanceOf(SQLException.class)
				.hasMessageContaining("CREATE TABLE NOPENOPENOPE");
	}

	public static DataSource createH2DataSource(final String url,
	                                            final String username,
	                                            final String password) {
		return JdbcConnectionPool.create(url, username, password);
	}

}