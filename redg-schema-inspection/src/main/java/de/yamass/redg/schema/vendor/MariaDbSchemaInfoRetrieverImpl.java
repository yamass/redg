package de.yamass.redg.schema.vendor;

import de.yamass.redg.schema.model.Constraint;
import de.yamass.redg.schema.model.ConstraintType;
import de.yamass.redg.schema.model.Udt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MariaDbSchemaInfoRetrieverImpl implements SchemaInfoRetriever {

	@Override
	public List<Constraint> getConstraints(Connection connection, String schema) throws SQLException {
		// Query UNIQUE constraints
		String uniqueConstraintsQuery = """
				SELECT 
				    CONSTRAINT_NAME
				FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
				WHERE CONSTRAINT_SCHEMA = ?
				    AND CONSTRAINT_TYPE = 'UNIQUE'
				""";
		
		// Query CHECK constraints
		// MariaDB stores CHECK constraints in TABLE_CONSTRAINTS, but the definition
		// might be in CHECK_CONSTRAINTS view (if available) or we need to parse SHOW CREATE TABLE
		String checkConstraintsQuery = """
				SELECT 
				    CONSTRAINT_NAME
				FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
				WHERE CONSTRAINT_SCHEMA = ?
				    AND CONSTRAINT_TYPE = 'CHECK'
				""";
		
		List<Constraint> constraints = new ArrayList<>();
		
		// Get UNIQUE constraints
		try (PreparedStatement ps = connection.prepareStatement(uniqueConstraintsQuery)) {
			ps.setString(1, schema);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String constraintName = rs.getString("CONSTRAINT_NAME");
					String definition = getUniqueConstraintDefinition(connection, schema, constraintName);
					constraints.add(new Constraint(
							schema,
							constraintName,
							ConstraintType.UNIQUE,
							definition,
							false
					));
				}
			}
		}
		
		// Get CHECK constraints
		try (PreparedStatement ps = connection.prepareStatement(checkConstraintsQuery)) {
			ps.setString(1, schema);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String constraintName = rs.getString("CONSTRAINT_NAME");
					String checkClause = getCheckConstraintDefinition(connection, schema, constraintName);
					boolean partial = checkClause != null && checkClause.toLowerCase().contains("where");
					constraints.add(new Constraint(
							schema,
							constraintName,
							ConstraintType.CHECK,
							checkClause,
							partial
					));
				}
			}
		}
		
		return constraints;
	}
	
	private String getUniqueConstraintDefinition(Connection connection, String schema, String constraintName) throws SQLException {
		String uniqueQuery = """
				SELECT COLUMN_NAME
				FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
				WHERE CONSTRAINT_SCHEMA = ? 
				    AND CONSTRAINT_NAME = ?
				ORDER BY ORDINAL_POSITION
				""";
		try (PreparedStatement ps = connection.prepareStatement(uniqueQuery)) {
			ps.setString(1, schema);
			ps.setString(2, constraintName);
			
			List<String> columns = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					columns.add(rs.getString("COLUMN_NAME"));
				}
			}
			
			if (columns.isEmpty()) {
				return null;
			}
			
			return "UNIQUE (" + String.join(", ", columns) + ")";
		}
	}
	
	private String getCheckConstraintDefinition(Connection connection, String schema, String constraintName) throws SQLException {
		// Try to get from CHECK_CONSTRAINTS view first
		String checkQuery = """
				SELECT CHECK_CLAUSE
				FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
				WHERE CONSTRAINT_SCHEMA = ?
				    AND CONSTRAINT_NAME = ?
				""";
		try (PreparedStatement ps = connection.prepareStatement(checkQuery)) {
			ps.setString(1, schema);
			ps.setString(2, constraintName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String checkClause = rs.getString("CHECK_CLAUSE");
					if (checkClause != null) {
						// Remove backticks from column names in the CHECK clause
						// MariaDB returns: `column_name` > 0, we want: column_name > 0
						checkClause = checkClause.replaceAll("`([^`]+)`", "$1");
					}
					return checkClause;
				}
			}
		} catch (SQLException e) {
			// CHECK_CONSTRAINTS view might not exist in all MariaDB versions
			// Fall through to return null
		}
		return null;
	}

	@Override
	public List<Udt> getUdts(Connection connection, String schema) throws SQLException {
		return List.of(); // TODO
	}

	@Override
	public int getArrayDimensions(Connection connection, String schema, String tableName, String columnName) throws SQLException {
		return 0; // TODO
	}
}
