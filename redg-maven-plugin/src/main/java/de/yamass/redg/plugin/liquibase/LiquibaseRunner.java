package de.yamass.redg.plugin.liquibase;

import java.sql.Connection;
import java.util.Objects;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

public class LiquibaseRunner {

	private final Log log;
	private final String changeLogFile;
	private final Connection connection;

	public LiquibaseRunner(Log log, String changeLogFile, Connection connection) {
		this.connection = Objects.requireNonNull(connection);
		this.log = Objects.requireNonNull(log);
		this.changeLogFile = Objects.requireNonNull(changeLogFile);
	}

	public void run() throws MojoExecutionException{
		log.info("Running Liquibase");

		try {
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(connection));
			Liquibase liquibase = new Liquibase(changeLogFile, new FileSystemResourceAccessor(), database);
			liquibase.update("");
			
		} catch (LiquibaseException e) {
			throw new MojoExecutionException("Problem during Liquibase execution",e);
		}
		

	}

}
