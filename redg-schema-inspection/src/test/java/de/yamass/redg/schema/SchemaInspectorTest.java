package de.yamass.redg.schema;

import de.yamass.redg.DatabaseType;
import de.yamass.redg.schema.inspector.SchemaInspector;
import de.yamass.redg.schema.model.*;
import de.yamass.redg.testing.Databases;
import de.yamass.redg.testing.DbContext;
import de.yamass.redg.testing.DbTest;
import de.yamass.redg.testing.Scripts;
import org.junit.jupiter.api.TestTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

import static de.yamass.redg.DatabaseType.MARIADB;
import static de.yamass.redg.DatabaseType.POSTGRES;
import static org.assertj.core.api.Assertions.assertThat;

@DbTest
class SchemaInspectorTest {

	@DbContext
	private DatabaseType databaseType;
	@DbContext
	private DataSource dataSource;

	@TestTemplate
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/column-nullability.sql")
	void extractsColumnNullability() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Table table = result.findTableOrThrow("public", "column_nullability_table");
		Column nullable = table.findColumnOrThrow("nullable_column");
		Column notNullable = table.findColumnOrThrow("not_nullable_column");

		assertThat(nullable.nullable()).isTrue();
		assertThat(notNullable.nullable()).isFalse();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/column-uniqueness.sql")
	void extractsColumnUniqueness() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Table table = result.findTableOrThrow("public", "column_uniqueness_table");
		assertThat(table.findColumnOrThrow("pk_column").unique()).isTrue();
		assertThat(table.findColumnOrThrow("single_column_unique").unique()).isTrue();
		assertThat(table.findColumnOrThrow("multi_column_unique_part_a").unique()).isFalse();
		assertThat(table.findColumnOrThrow("multi_column_unique_part_b").unique()).isFalse();
		assertThat(table.findColumnOrThrow("non_unique_column").unique()).isFalse();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/foreign-key.sql")
	void extractsForeignKeyRelationships() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Table source = result.findTableOrThrow("public", "fk_source_table");

		Table target = result.findTableOrThrow("public", "fk_target_table");

		assertThat(source.outgoingForeignKeys()).hasSize(1);
		ForeignKey outgoing = source.outgoingForeignKeys().get(0);
		assertThat(outgoing.targetTable()).isEqualTo(target);
		assertThat(outgoing.columns()).hasSize(1);
		ForeignKeyColumn column = outgoing.columns().get(0);
		assertThat(column.sourceColumn().name()).isEqualTo("target_id_fk");
		assertThat(column.targetColumn().name()).isEqualTo("target_id");

		assertThat(target.incomingForeignKeys()).hasSize(1);
		ForeignKey incoming = target.incomingForeignKeys().get(0);
		assertThat(incoming.sourceTable()).isEqualTo(source);
		assertThat(incoming.targetTable()).isEqualTo(target);
		assertThat(incoming.columns()).hasSize(1);
		ForeignKeyColumn incomingColumn = incoming.columns().get(0);
		assertThat(incomingColumn.sourceColumn().name()).isEqualTo("target_id_fk");
		assertThat(incomingColumn.targetColumn().name()).isEqualTo("target_id");
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/foreign-key-composite.sql")
	void extractsCompositeForeignKeyRelationships() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Table source = result.findTableOrThrow("public", "fk_composite_source_table");
		Table target = result.findTableOrThrow("public", "fk_composite_target_table");

		assertThat(source.outgoingForeignKeys()).hasSize(1);
		ForeignKey outgoing = source.outgoingForeignKeys().get(0);
		assertThat(outgoing.targetTable()).isEqualTo(target);
		assertThat(outgoing.columns()).hasSize(2);
		
		ForeignKeyColumn firstColumn = outgoing.columns().get(0);
		assertThat(firstColumn.sourceColumn().name()).isEqualTo("target_part_a_fk");
		assertThat(firstColumn.targetColumn().name()).isEqualTo("target_part_a");
		
		ForeignKeyColumn secondColumn = outgoing.columns().get(1);
		assertThat(secondColumn.sourceColumn().name()).isEqualTo("target_part_b_fk");
		assertThat(secondColumn.targetColumn().name()).isEqualTo("target_part_b");

		assertThat(target.incomingForeignKeys()).hasSize(1);
		ForeignKey incoming = target.incomingForeignKeys().get(0);
		assertThat(incoming.sourceTable()).isEqualTo(source);
		assertThat(incoming.targetTable()).isEqualTo(target);
		assertThat(incoming.columns()).hasSize(2);
		ForeignKeyColumn incomingFirstColumn = incoming.columns().get(0);
		assertThat(incomingFirstColumn.sourceColumn().name()).isEqualTo("target_part_a_fk");
		assertThat(incomingFirstColumn.targetColumn().name()).isEqualTo("target_part_a");
		ForeignKeyColumn incomingSecondColumn = incoming.columns().get(1);
		assertThat(incomingSecondColumn.sourceColumn().name()).isEqualTo("target_part_b_fk");
		assertThat(incomingSecondColumn.targetColumn().name()).isEqualTo("target_part_b");
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/constraints.sql")
	void extractsConstraints() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		boolean uniqueConstraintDetected = result.constraints().stream().anyMatch(constraintMatches(
				"constraint_unique_column_unique",
				ConstraintType.UNIQUE,
				"UNIQUE (constraint_unique_column)"
		));
		boolean checkConstraintDetected = result.constraints().stream().anyMatch(constraintMatches(
				"constraint_positive_value_check",
				ConstraintType.CHECK,
				"constraint_positive_value > 0"
		));

		assertThat(uniqueConstraintDetected).isTrue();
		assertThat(checkConstraintDetected).isTrue();
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/udt.sql")
	void extractsUserDefinedTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		boolean udtDetected = result.udts().stream().anyMatch(udt ->
				"udt_status_enum".equalsIgnoreCase(udt.name()) &&
						"e".equalsIgnoreCase(udt.type())
		);

		assertThat(udtDetected).isTrue();
	}

	private SchemaInspectionResult inspectPublicSchema() throws SQLException {
		SchemaInspector schemaInspector = new SchemaInspector(databaseType, dataSource);
		return schemaInspector.inspectSchema("public");
	}

	private static java.util.function.Predicate<Constraint> constraintMatches(String name, ConstraintType type, String expectedFragment) {
		return constraint -> constraint.name().equalsIgnoreCase(name)
				&& constraint.type() == type
				&& constraint.definition() != null
				&& constraint.definition().toUpperCase().contains(expectedFragment.toUpperCase());
	}
}