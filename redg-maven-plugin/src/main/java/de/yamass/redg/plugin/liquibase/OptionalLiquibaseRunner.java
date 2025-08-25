package de.yamass.redg.plugin.liquibase;

import java.sql.Connection;
import java.util.Objects;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.sisu.Nullable;

import de.yamass.redg.plugin.util.ClassAvailabilityChecker;

public class OptionalLiquibaseRunner {

	private static final String LIQUIBASE_FQCN = "liquibase.Liquibase";
	private final String changeLogFileOrNull;

	public OptionalLiquibaseRunner(@Nullable String changeLogFileOrNull) {
		this.changeLogFileOrNull = changeLogFileOrNull;
	}

	public void execute(Log log, Connection connection) throws MojoExecutionException {
		if (changeLogFileOrNull != null) {
			if (new ClassAvailabilityChecker(LIQUIBASE_FQCN).isAvailable()) {
				log.debug("Liquibase found on classpath");
				new LiquibaseRunner(Objects.requireNonNull(log), changeLogFileOrNull, connection).run();
			}else {
				log.warn("Even though a liquibaseChangeLogFile is provided, Liquibase cannot execute as it is not found on the classpath. Please add a dependency accordingly.");
			}
		}else {
			// skip silently
		}
	}
}
