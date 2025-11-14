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

public class PostgresSchemaInfoRetrieverImpl implements SchemaInfoRetriever {

	@Override
	public List<Constraint> getConstraints(Connection connection, String schema) throws SQLException {
		String constraintsQuery = """
				SELECT c.conname, c.contype, pg_get_constraintdef(c.oid) AS def
				FROM pg_constraint c
				JOIN pg_namespace n ON n.oid = c.connamespace
				WHERE n.nspname = ?
				""";
		try (PreparedStatement ps = connection.prepareStatement(constraintsQuery)) {
			ps.setString(1, schema);

			List<Constraint> constraints = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String def = rs.getString("def");
					boolean partial = def != null && def.toLowerCase().contains("where");
					ConstraintType type = ConstraintType.fromDatabaseCode(rs.getString("contype"));
					constraints.add(new Constraint(
							schema,
							rs.getString("conname"),
							type,
							def,
							partial
					));
				}
			}
			return constraints;
		}
	}

	@Override
	public List<Udt> getUdts(Connection connection, String schema) throws SQLException {
		String udtQuery = """
				SELECT typname, typtype, typcategory
				FROM pg_type t
				JOIN pg_namespace n ON n.oid = t.typnamespace
				WHERE n.nspname = ?
				""";
		List<Udt> udts = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(udtQuery)) {
			ps.setString(1, schema);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					udts.add(new Udt(
							schema,
							rs.getString("typname"),
							rs.getString("typtype"),
							rs.getString("typcategory")
					));
				}
			}
		}
		return udts;
	}
}
