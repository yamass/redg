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

package de.yamass.redg.generator.extractor.utils;

/*
 * Taken with gratitude from https://github.com/BenoitDuffez/ScriptRunner
 */
/*
 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 * GPSHansl, 06.08.2015: regex for delimiter, rearrange comment/delimiter detection, remove some ide warnings.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool to run extractor scripts
 */
public class ScriptRunner {

	private static final Logger LOG = LoggerFactory.getLogger(ScriptRunner.class);
	private static final String DEFAULT_DELIMITER = ";";
	/**
	 * regex to detect delimiter.
	 * ignores spaces, allows delimiter in comment, allows an equals-sign
	 */
	public static final Pattern delimP = Pattern.compile("^\\s*(--)?\\s*delimiter\\s*=?\\s*([^\\s]+)+\\s*.*$", Pattern.CASE_INSENSITIVE);

	private final Connection connection;

	private final boolean stopOnError;
	private final boolean autoCommit;

	private String delimiter = DEFAULT_DELIMITER;
	private boolean fullLineDelimiter = false;

	/**
	 * Default constructor
	 *
	 * @param connection  the JDBC connection
	 * @param autoCommit  whether auto commit should be turned on or not
	 * @param stopOnError whether to stop execution when encountering an error or not
	 */
	public ScriptRunner(Connection connection, boolean autoCommit,
	                    boolean stopOnError) {
		this.connection = connection;
		this.autoCommit = autoCommit;
		this.stopOnError = stopOnError;
	}

	public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
		this.delimiter = delimiter;
		this.fullLineDelimiter = fullLineDelimiter;
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter)
	 *
	 * @param reader - the source of the script
	 * @throws IOException  Gets thrown when reading the script fails
	 * @throws SQLException Gets thrown when the SQL execution fails
	 */
	public void runScript(Reader reader) throws IOException, SQLException {
		try {
			boolean originalAutoCommit = connection.getAutoCommit();
			try {
				if (originalAutoCommit != this.autoCommit) {
					connection.setAutoCommit(this.autoCommit);
				}
				runScript(connection, reader);
			} finally {
				connection.setAutoCommit(originalAutoCommit);
			}
		} catch (IOException | SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error running script.  Cause: " + e, e);
		}
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter) using the
	 * connection passed in
	 *
	 * @param conn   - the connection to use for the script
	 * @param reader - the source of the script
	 * @throws SQLException if any SQL errors occur
	 * @throws IOException  if there is an error reading from the Reader
	 */
	private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
		StringBuffer command = null;
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				final Matcher delimMatch = delimP.matcher(trimmedLine);
				if (trimmedLine.length() < 1
						|| trimmedLine.startsWith("//")) {
					// Do nothing
				} else if (delimMatch.matches()) {
					setDelimiter(delimMatch.group(2), false);
				} else if (trimmedLine.startsWith("--")) {
					LOG.debug(trimmedLine);
				} else if (trimmedLine.length() < 1
						|| trimmedLine.startsWith("--")) {
					// Do nothing
				} else if (!fullLineDelimiter
						&& trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter
						&& trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line
							.lastIndexOf(getDelimiter())));
					command.append(" ");
					this.execCommand(conn, command.toString(), lineReader);
					command = null;
				} else {
					command.append(line);
					command.append("\n");
				}
			}
			if (command != null) {
				this.execCommand(conn, command.toString(), lineReader);
			}
			if (!autoCommit) {
				conn.commit();
			}
		} catch (Exception e) {
			LOG.error("Error executing '{}': {}", command, e.getMessage(), e);
			if (!autoCommit) {
				try {
					conn.rollback();
				} catch (Exception e2) {
					LOG.error("Error during rollback.", e2);
				}
			}
			throw new IOException(String.format("Error executing '%s': %s", command, e.getMessage()), e);
		}
	}

	private void execCommand(Connection conn, String command,
	                         LineNumberReader lineReader) throws SQLException {

		Statement statement = conn.createStatement();

		LOG.debug("Executing: {}", command);

		boolean hasResults = false;
		try {
			hasResults = statement.execute(command);
		} catch (SQLException e) {
			final String errText = String.format("Error executing '%s' (line %d): %s", command, lineReader.getLineNumber(), e.getMessage());
			LOG.error(errText);
			if (stopOnError) {
				throw new SQLException(errText, e);
			}
		}

		if (autoCommit && !conn.getAutoCommit()) {
			conn.commit();
		}

		ResultSet rs = statement.getResultSet();
		if (hasResults && rs != null) {
			StringBuilder logBuffer = new StringBuilder();
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			for (int i = 1; i <= cols; i++) {
				String name = md.getColumnLabel(i);
				logBuffer.append(name + "\t");
			}
			logBuffer.append("\n");
			while (rs.next()) {
				for (int i = 1; i <= cols; i++) {
					String value = rs.getString(i);
					logBuffer.append(value + "\t");
				}
				logBuffer.append("\n");
			}
			LOG.debug("Result: {}", logBuffer);
		}

		try {
			statement.close();
		} catch (Exception e) {
			// Ignore to workaround a bug in Jakarta DBCP
		}
	}

	private String getDelimiter() {
		return delimiter;
	}
}