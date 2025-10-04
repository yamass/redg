package de.yamass.redg.generator.testutil;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Manages shared testcontainers across the module.
 */
public class TestDatabaseContainers {

    private static PostgreSQLContainer<?> postgres;
    private static MariaDBContainer<?> mariadb;

    public static JdbcDatabaseContainer<?> postgres() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
            postgres.start();
        }
        return postgres;
    }

    public static JdbcDatabaseContainer<?> mariadb() {
        if (mariadb == null) {
            mariadb = new MariaDBContainer<>(DockerImageName.parse("mariadb:lts"));
            mariadb.start();
        }
        return mariadb;
    }

}