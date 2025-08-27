/*
 * Copyright Yann Massard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.runtime;

import de.yamass.redg.runtime.defaultvalues.DefaultDefaultValueStrategy;
import de.yamass.redg.runtime.defaultvalues.DefaultValueStrategy;
import de.yamass.redg.runtime.defaultvalues.pluggable.PluggableDefaultValueStrategy;
import de.yamass.redg.runtime.dummy.DefaultDummyFactory;
import de.yamass.redg.runtime.dummy.DummyFactory;
import de.yamass.redg.runtime.insertvalues.DefaultSQLValuesFormatter;
import de.yamass.redg.runtime.insertvalues.SQLValuesFormatter;
import de.yamass.redg.runtime.mocks.MockEntity1;
import de.yamass.redg.runtime.mocks.MockEntity2;
import de.yamass.redg.runtime.mocks.MockRedG;
import de.yamass.redg.runtime.transformer.DefaultPreparedStatementParameterSetter;
import de.yamass.redg.runtime.transformer.PreparedStatementParameterSetter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class AbstractRedGTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDefaultValueStrategySetGet() {
        PluggableDefaultValueStrategy strategy = new PluggableDefaultValueStrategy();
        MockRedG mockRedG = new MockRedG();

        assertTrue(mockRedG.getDefaultValueStrategy() instanceof DefaultDefaultValueStrategy);
        mockRedG.setDefaultValueStrategy(strategy);
        assertEquals(strategy, mockRedG.getDefaultValueStrategy());
        mockRedG.setDefaultValueStrategy(null);
        assertTrue(mockRedG.getDefaultValueStrategy() instanceof DefaultDefaultValueStrategy);
    }

    @Test
    public void testGetEntities() throws Exception {
        MockRedG mockRedG = new MockRedG();
        RedGEntity e = new MockEntity1();
        mockRedG.addEntity(e);
        assertThat(mockRedG.getEntities()).containsExactly(e);
    }

    @Test
    public void testGetEntities_Immutable() throws Exception {
        this.expectedException.expect(UnsupportedOperationException.class);
        MockRedG mockRedG = new MockRedG();
        RedGEntity e = new MockEntity1();
        mockRedG.addEntity(e);
        mockRedG.getEntities().clear();
    }

    @Test
    public void testSetDummyFactory() throws Exception {
        MockRedG mockRedG = new MockRedG();
        DummyFactory df = new DefaultDummyFactory();
        mockRedG.setDummyFactory(df);
        assertThat(mockRedG.getDummyFactory()).isEqualTo(df);
        mockRedG.setDummyFactory(null);
        assertThat(mockRedG.getDummyFactory()).isInstanceOf(DefaultDummyFactory.class);
    }

    @Test
    public void testSetDummyFactory_AfterEntityAdd() throws Exception {
        this.expectedException.expect(IllegalStateException.class);
        MockRedG mockRedG = new MockRedG();
        mockRedG.addEntity(new MockEntity1());
        mockRedG.setDummyFactory(null);
    }

    @Test
    public void testSetPSPS() throws Exception {
        MockRedG mockRedG = new MockRedG();
        PreparedStatementParameterSetter psps = new DefaultPreparedStatementParameterSetter();
        mockRedG.setPreparedStatementParameterSetter(psps);
        assertThat(mockRedG.getPreparedStatementParameterSetter()).isEqualTo(psps);
        mockRedG.setPreparedStatementParameterSetter(null);
        assertThat(mockRedG.getPreparedStatementParameterSetter()).isInstanceOf(PreparedStatementParameterSetter.class);
    }

    @Test
    public void testSetPSPS_AfterEntityAdd() throws Exception {
        this.expectedException.expect(IllegalStateException.class);
        MockRedG mockRedG = new MockRedG();
        mockRedG.addEntity(new MockEntity1());
        mockRedG.setPreparedStatementParameterSetter(null);
    }

    @Test
    public void testSetDVS() throws Exception {
        MockRedG mockRedG = new MockRedG();
        DefaultValueStrategy dvs = new DefaultDefaultValueStrategy();
        mockRedG.setDefaultValueStrategy(dvs);
        assertThat(mockRedG.getDefaultValueStrategy()).isEqualTo(dvs);
        mockRedG.setDefaultValueStrategy(null);
        assertThat(mockRedG.getDefaultValueStrategy()).isInstanceOf(DefaultDefaultValueStrategy.class);
    }

    @Test
    public void testSetDVS_AfterEntityAdd() throws Exception {
        this.expectedException.expect(IllegalStateException.class);
        MockRedG mockRedG = new MockRedG();
        mockRedG.addEntity(new MockEntity1());
        mockRedG.setDefaultValueStrategy(null);
    }

    @Test
    public void testSetSVF() throws Exception {
        MockRedG mockRedG = new MockRedG();
        SQLValuesFormatter svf = new DefaultSQLValuesFormatter();
        mockRedG.setSqlValuesFormatter(svf);
        assertThat(mockRedG.getSqlValuesFormatter()).isEqualTo(svf);
        mockRedG.setSqlValuesFormatter(null);
        assertThat(mockRedG.getSqlValuesFormatter()).isInstanceOf(DefaultSQLValuesFormatter.class);
    }

    @Test
    public void testSetSVF_AfterEntityAdd() throws Exception {
        this.expectedException.expect(IllegalStateException.class);
        MockRedG mockRedG = new MockRedG();
        mockRedG.addEntity(new MockEntity1());
        mockRedG.setSqlValuesFormatter(null);
    }

    @Test
    public void testInsertValuesFormatterSetGet() {
        MockRedG mockRedG = new MockRedG();
        DefaultSQLValuesFormatter formatter = new DefaultSQLValuesFormatter();

        assertTrue(mockRedG.getSqlValuesFormatter() instanceof DefaultSQLValuesFormatter);
        assertNotEquals(formatter, mockRedG.getSqlValuesFormatter());
        mockRedG.setSqlValuesFormatter(formatter);
        assertEquals(formatter, mockRedG.getSqlValuesFormatter());
        mockRedG.setSqlValuesFormatter(null);
        assertTrue(mockRedG.getSqlValuesFormatter() instanceof DefaultSQLValuesFormatter);
        assertNotEquals(formatter, mockRedG.getSqlValuesFormatter());
    }

    @Test
    public void testFindSingleEntity() {
        MockRedG mockRedG = new MockRedG();
        MockEntity1 entity1 = new MockEntity1();
        MockEntity2 entity2 = new MockEntity2();

        mockRedG.addEntity(entity1);
        mockRedG.addEntity(entity2);

        assertEquals(entity1, mockRedG.findSingleEntity(MockEntity1.class, e -> e.toString().equals("MockEntity1")));
        assertEquals(entity2, mockRedG.findSingleEntity(MockEntity2.class, e -> e.toString().equals("MockEntity2")));

        boolean exceptionThrown = false;
        try {
            assertNull(mockRedG.findSingleEntity(MockEntity1.class, e -> false));
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testFindAllObjects() {
        MockRedG mockRedG = new MockRedG();
        List<MockEntity1> entities = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());
        entities.forEach(mockRedG::addEntity);

        assertEquals(entities, mockRedG.findEntities(MockEntity1.class, e -> true));
        assertTrue(mockRedG.findEntities(MockEntity2.class, e -> true).isEmpty());
    }

    @Test
    public void testGenerateInsertStatements() {
        MockRedG mockRedG = new MockRedG();
        List<MockEntity1> entities = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());
        List<String> results = IntStream.rangeClosed(1, 20).mapToObj(i -> "INSERT").collect(Collectors.toList());
        entities.forEach(mockRedG::addEntity);

        assertEquals(results, mockRedG.generateSQLStatements());
    }

    @Test
    public void testInsertConnection() throws Exception {
        Connection connection = getConnection("conn");
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

        List<MockEntity1> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());

        MockRedG mockRedG = new MockRedG();
        gObjects.forEach(mockRedG::addEntity);

        mockRedG.insertDataIntoDatabase(connection);

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TEST");
        rs.next();
        assertEquals(20, rs.getInt(1));
    }

    private Connection getConnection(String suffix) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:mem:abstractredgtest-" + suffix, "", "");
    }

}
