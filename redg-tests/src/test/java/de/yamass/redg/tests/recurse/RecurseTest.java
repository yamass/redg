package de.yamass.redg.tests.recurse;

import de.yamass.redg.generated.recurse.GTreeElement;
import de.yamass.redg.generated.recurse.RedG;
import de.yamass.redg.generator.extractor.DatabaseManager;
import de.yamass.redg.tests.Helpers;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

class RecurseTest {

    @BeforeEach
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        final DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-recurse", "", "");
        Assertions.assertNotNull(dataSource);
        final File sqlFile = Helpers.getResourceAsFile("recurse-schema.sql");
        DatabaseManager.executePreparationScripts(dataSource, new File[]{sqlFile});
    }

    @Test
    void test() throws Exception {
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
        Assertions.assertFalse(rs.next());
    }

    private void prepareTestData(final RedG redg) {
        // create root element
        GTreeElement root = redg.addTreeElement(redg.entitySelfReference()).someValue("Root");
        redg.addTreeElement(root).someValue("Child 1");
        GTreeElement child = redg.addTreeElement(root).someValue("Child 2");
        redg.addTreeElement(child).someValue("Child 3");

    }
}
