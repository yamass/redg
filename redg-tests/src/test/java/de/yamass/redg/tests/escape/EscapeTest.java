package de.yamass.redg.tests.escape;

import de.yamass.redg.generated.escape.RedG;
import de.yamass.redg.tests.Helpers;
import de.yamass.redg.util.ScriptRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

class EscapeTest {

    @BeforeEach
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-escape", "", "");
        Assertions.assertNotNull(dataSource);
        final File sqlFile = Helpers.getResourceAsFile("escape-schema.sql");
        ScriptRunner.executeScripts(dataSource, new File[]{sqlFile});
    }

    @Test
    void test() throws Exception {
        final Connection connection = JdbcConnectionPool.create("jdbc:h2:mem:redg-escape", "", "")
                .getConnection();

        final RedG redG = new RedG();
        prepareTestData(redG);

        redG.insertDataIntoDatabase(connection);

        checkData(connection);
    }

    private void checkData(Connection connection) throws Exception {
        final Statement statement = connection.createStatement();
        final ResultSet rs = statement.executeQuery("select * from \"T1\"");
        rs.next();
        Helpers.assertResultSet(rs, 0, "Test");
        rs.next();
        Helpers.assertResultSet(rs, 1, "Hello");
    }

    private void prepareTestData(RedG redG) {
        redG.addT1().name("Test");
        redG.addT1().name("Hello");
    }
}
