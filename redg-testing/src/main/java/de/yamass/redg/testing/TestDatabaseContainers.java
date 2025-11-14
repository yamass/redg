package de.yamass.redg.testing;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Manages shared testcontainers across the module.
 */
public class TestDatabaseContainers {

	public static final String SCHEMA_NAME = "public";

    private static PostgreSQLContainer<?> postgres;
    private static MariaDBContainer<?> mariadb;

    public static JdbcDatabaseContainer<?> postgres() {
        if (postgres == null) {
	        //noinspection resource
	        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
		            .withReuse(true);
            postgres.start();
        }
        return postgres;
    }

    public static JdbcDatabaseContainer<?> mariadb() {
        if (mariadb == null) {
	        //noinspection resource
	        mariadb = new MariaDBContainer<>(DockerImageName.parse("mariadb:lts"))
		            .withDatabaseName(SCHEMA_NAME)
			        .withReuse(true);
            mariadb.start();
        }
        return mariadb;
    }

}