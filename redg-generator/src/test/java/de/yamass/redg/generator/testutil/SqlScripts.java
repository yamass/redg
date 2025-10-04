package de.yamass.redg.generator.testutil;

import de.yamass.redg.generator.DatabaseType;
import de.yamass.redg.generator.Helpers;
import de.yamass.redg.generator.extractor.DatabaseManager;
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
			DatabaseManager.executeScript(dataSource, () -> Helpers.getResourceAsReader(databaseSpecificResourcePath));
		} else {
			LOGGER.info("Executing generic script: {}", databaseSpecificResourcePath);
			DatabaseManager.executeScript(dataSource, () -> Helpers.getResourceAsReader(resourcePath));
		}
	}

	public static void executeScripts(DataSource dataSource, DatabaseType databaseType, List<String> resourcePath) throws IOException, SQLException {
		for (String path : resourcePath) {
			executeScript(dataSource, databaseType, path);
		}
	}

}
