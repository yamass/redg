package de.yamass.redg.tests.standard;

import de.yamass.redg.generated.standard.GReservation;
import de.yamass.redg.generated.standard.GRestaurant;
import de.yamass.redg.generated.standard.RedG;
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
import java.sql.Timestamp;

class StandardTest {

    @BeforeEach
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-standard", "", "");
        Assertions.assertNotNull(dataSource);
        final File sqlFile = Helpers.getResourceAsFile("standard-schema.sql");
        DatabaseManager.executeScripts(dataSource, new File[]{sqlFile});
    }

    @Test
    void test() throws Exception {
        final Connection connection = JdbcConnectionPool.create("jdbc:h2:mem:redg-standard", "", "")
                .getConnection();

        final RedG redG = new RedG();
        prepareTestData(redG);

        redG.insertDataIntoDatabase(connection);

        checkData(connection);
    }

    private void checkData(Connection connection) throws Exception {
        final Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select * from RESTAURANT");
        rs.next();
        Helpers.assertResultSet(rs, 0, "Susan's Steakhouse");
        Assertions.assertFalse(rs.next());

        rs = statement.executeQuery("select count(*) from GUEST");
        rs.next();
        Helpers.assertResultSet(rs, 1);
        Assertions.assertFalse(rs.next());

        rs = statement.executeQuery("select * from WAITER");
        rs.next();
        Helpers.assertResultSet(rs, 0, "Sally", 0);
        rs.next();
        Helpers.assertResultSet(rs, 1, "Stefan", 0);
        Assertions.assertFalse(rs.next());

        rs = statement.executeQuery("select * from RESERVATION order by TIME");
        rs.next();
        Helpers.assertResultSet(rs, 0, 0, new Timestamp(1L));
        rs.next();
        Helpers.assertResultSet(rs, 0, 0, new Timestamp(1234567890L));
        Assertions.assertFalse(rs.next());

        rs = statement.executeQuery("select * from WAITER_RESERVATION order by TIME");
        rs.next();
        Helpers.assertResultSet(rs, 1, 0, 0, new Timestamp(1L));
        rs.next();
        Helpers.assertResultSet(rs, 0, 0, 0, new Timestamp(1234567890L));
        Assertions.assertFalse(rs.next());
    }

    private void prepareTestData(final RedG redg) {
        GReservation reservation = redg.addReservation(
                redg.addRestaurant().name("Susan's Steakhouse"),
                redg.dummyGuest()
        ).time(new Timestamp(1234567890L));
        redg.addWaiterReservation(redg.addWaiter().name("Sally"), reservation);

        GReservation reservation2 = redg.addReservation(
                redg.findSingleEntity(GRestaurant.class, r -> r.name().startsWith("Susan")),
                redg.dummyGuest()
        );
        redg.addWaiterReservation(redg.addWaiter().name("Stefan"), reservation2);
    }
}
