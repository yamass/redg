package com.btc.redg.tests.escape;

import com.btc.redg.generated.escape.RedG;
import com.btc.redg.generator.extractor.DatabaseManager;
import com.btc.redg.tests.Helpers;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;

public class EscapeTest {

    @Before
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-escape", "", "");
        assertNotNull(dataSource);
        final File sqlFile = Helpers.getResourceAsFile("escape-schema.sql");
        DatabaseManager.executePreparationScripts(dataSource, new File[]{sqlFile});
    }

    @Test
    public void test() throws Exception {
        final Connection connection = JdbcConnectionPool.create("jdbc:h2:mem:redg-escape", "", "")
                .getConnection();

        final RedG redG = new RedG();
        prepareTestData(redG);

        redG.insertDataIntoDatabase(connection);

        checkData(connection);
    }

    private void checkData(Connection connection) throws Exception {
        final Statement statement = connection.createStatement();
        final ResultSet rs = statement.executeQuery("select * from \"TABLE\"");
        rs.next();
        Helpers.assertResultSet(rs, 0, "Test");
        rs.next();
        Helpers.assertResultSet(rs, 1, "Hello");
    }

    private void prepareTestData(RedG redG) {
        redG.addTable().name("Test");
        redG.addTable().name("Hello");
    }
}
