package com.btc.redg.tests.recurse;

import com.btc.redg.generated.recurse.GTreeElement;
import com.btc.redg.generated.recurse.RedG;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class RecurseTest {

    @Before
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        final DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-recurse", "", "");
        assertNotNull(dataSource);
        final File sqlFile = Helpers.getResourceAsFile("recurse-schema.sql");
        DatabaseManager.executePreparationScripts(dataSource, new File[]{sqlFile});
    }

    @Test
    public void test() throws Exception {
        final Connection connection = JdbcConnectionPool.create("jdbc:h2:mem:redg-recurse", "", "")
                .getConnection();

        final RedG redG = new RedG();
        prepareTestData(redG);

        redG.insertDataIntoDatabase(connection);

        checkData(connection);
    }

    private void checkData(Connection connection) throws Exception {
        final Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select * from TREE_ELEMENT");
        rs.next();
        Helpers.assertResultSet(rs, 0, "Root", 0);
        rs.next();
        Helpers.assertResultSet(rs, 1, "Child 1", 0);
        rs.next();
        Helpers.assertResultSet(rs, 2, "Child 2", 0);
        rs.next();
        Helpers.assertResultSet(rs, 3, "Child 3", 2);
        assertFalse(rs.next());
    }

    private void prepareTestData(final RedG redg) {
        // create root element
        GTreeElement root = redg.addTreeElement(redg.entitySelfReference()).value("Root");
        redg.addTreeElement(root).value("Child 1");
        GTreeElement child = redg.addTreeElement(root).value("Child 2");
        redg.addTreeElement(child).value("Child 3");

    }
}
