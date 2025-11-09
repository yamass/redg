package de.yamass.redg.testing;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.util.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SqlScripts {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqlScripts.class);

	public static void executeScript(DataSource dataSource, DatabaseType databaseType, String resourcePath) throws IOException, SQLException {
		int dotIndex = resourcePath.lastIndexOf('.');

		String root = resourcePath.substring(0, dotIndex);
		String suffix = resourcePath.substring(dotIndex);

		String databaseSpecificResourcePath = root + "." + databaseType.name().toLowerCase() + suffix;

		if (Helpers.resourceExists(databaseSpecificResourcePath)) {
			LOGGER.info("Executing database specific script: {}", databaseSpecificResourcePath);
			ScriptRunner.executeScript(dataSource, () -> Helpers.getResourceAsReader(databaseSpecificResourcePath));
		} else {
			LOGGER.info("Executing generic script: {}", databaseSpecificResourcePath);
			ScriptRunner.executeScript(dataSource, () -> Helpers.getResourceAsReader(resourcePath));
		}
	}

	public static void executeScripts(DataSource dataSource, DatabaseType databaseType, List<String> resourcePath) throws IOException, SQLException {
		for (String path : resourcePath) {
			executeScript(dataSource, databaseType, path);
		}
	}

}
