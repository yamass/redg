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

package de.yamass.redg.runtime.jdbc;

import de.yamass.redg.runtime.ExistingEntryMissingException;
import de.yamass.redg.runtime.InsertionFailedException;
import de.yamass.redg.runtime.mocks.ExistingMockEntity1;
import de.yamass.redg.runtime.mocks.MockEntity1;
import de.yamass.redg.runtime.mocks.MockEntity3;
import de.yamass.redg.runtime.mocks.MockEntity4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedGDatabaseUtilTest {

	@Test
	void testInsertDataIntoDatabase() throws Exception {
		Connection connection = getConnection("-idid");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

		List<MockEntity1> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());

		RedGDatabaseUtil.insertDataIntoDatabase(gObjects, connection);

		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TEST");
		rs.next();
		Assertions.assertEquals(20, rs.getInt(1));

	}

	@Test
	void testInsertDataIntoDatabase2() throws Exception {
		Connection connection = getConnection("-idid2");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

		List<MockEntity3> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity3()).collect(Collectors.toList());

		RedGDatabaseUtil.insertDataIntoDatabase(gObjects, connection);

		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TEST");
		rs.next();
		Assertions.assertEquals(20, rs.getInt(1));

	}

	@Test
	void testInsertDataIntoDatabase3() throws Exception {
		Connection connection = getConnection("-idid3");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

		List<MockEntity4> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity4()).collect(Collectors.toList());

		RedGDatabaseUtil.insertDataIntoDatabase(gObjects, connection);

		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TEST");
		rs.next();
		Assertions.assertEquals(40, rs.getInt(1));

	}

	@Test
	void testInsertDataIntoDatabase_FailPreparedStatement() throws Exception {
		Connection mockConnection = mock(Connection.class);
		when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Mock reason"));

		List<MockEntity1> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());

		assertThatThrownBy(() -> RedGDatabaseUtil.insertDataIntoDatabase(gObjects, mockConnection))
				.isInstanceOf(InsertionFailedException.class)
				.hasMessageContaining("Could not get prepared statement for class");
	}

	@Test
	void testInsertDataIntoDatabase_FailOnPreparedStatementSetValue() throws Exception {
		Connection mockConnection = mock(Connection.class);
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		Mockito.doThrow(new SQLException("Mock reason")).when(preparedStatement).setObject(anyInt(), any(), anyInt());
		when(mockConnection.prepareStatement(anyString())).thenReturn(preparedStatement);

		List<MockEntity1> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());

		assertThatThrownBy(() -> RedGDatabaseUtil.insertDataIntoDatabase(gObjects, mockConnection))
				.isInstanceOf(InsertionFailedException.class)
				.hasMessageContaining("Setting value for statement failed");
	}

	@Test
	void testInsertDataIntoDatabase_FailOnPreparedStatementExecute() throws Exception {
		Connection mockConnection = mock(Connection.class);
		PreparedStatement preparedStatement = mock(PreparedStatement.class);
		Mockito.doThrow(new SQLException("Mock reason")).when(preparedStatement).execute();
		when(mockConnection.prepareStatement(anyString())).thenReturn(preparedStatement);

		List<MockEntity1> gObjects = IntStream.rangeClosed(1, 20).mapToObj(i -> new MockEntity1()).collect(Collectors.toList());

		assertThatThrownBy(() -> RedGDatabaseUtil.insertDataIntoDatabase(gObjects, mockConnection))
				.isInstanceOf(InsertionFailedException.class)
				.hasMessageContaining("SQL execution failed");
	}

	private Connection getConnection(final String suffix) throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		return DriverManager.getConnection("jdbc:h2:mem:test-" + suffix, "", "");
	}

	@Test
	void testInsertExistingDataIntoDatabase() throws Exception {
		Connection connection = getConnection("-iedid");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

		stmt.execute("INSERT INTO TEST VALUES ('obj1')");
		RedGDatabaseUtil.insertDataIntoDatabase(Collections.singletonList(new ExistingMockEntity1()), connection);
	}

	@Test
	void testInsertExistingDataIntoDatabase_NotExisting() throws Exception {
		Connection connection = getConnection("-iedidm");
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE TEST (CONTENT VARCHAR2(50 CHARACTERS))");

		assertThatThrownBy(() -> RedGDatabaseUtil.insertDataIntoDatabase(Collections.singletonList(new ExistingMockEntity1()), connection))
				.isInstanceOf(ExistingEntryMissingException.class);
	}

	@Test
	void testConstructor() throws Exception {
		Constructor constructor = RedGDatabaseUtil.class.getDeclaredConstructor();
		Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));

		constructor.setAccessible(true);
		constructor.newInstance();
	}

}