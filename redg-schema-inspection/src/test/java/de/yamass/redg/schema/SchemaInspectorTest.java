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
import java.util.Optional;

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
	@Databases({POSTGRES, MARIADB})
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
	@Databases({POSTGRES, MARIADB})
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
	@Databases({POSTGRES, MARIADB})
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
	@Databases({POSTGRES, MARIADB})
	@Scripts("de/yamass/redg/schema/sql/constraints.sql")
	void extractsConstraints() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Optional<Constraint> uniqueConstraint = result.constraints().stream()
				.filter(c -> "constraint_unique_column_unique".equalsIgnoreCase(c.name()))
				.findFirst();
		assertThat(uniqueConstraint).isPresent();
		assertThat(uniqueConstraint.get()).satisfies(constraint -> {
			assertThat(constraint.type()).isEqualTo(ConstraintType.UNIQUE);
			assertThat(constraint.definition()).isNotNull();
			assertThat(constraint.definition().toUpperCase()).contains("UNIQUE (CONSTRAINT_UNIQUE_COLUMN)");
		});

		Optional<Constraint> checkConstraint = result.constraints().stream()
				.filter(c -> "constraint_positive_value_check".equalsIgnoreCase(c.name()))
				.findFirst();
		assertThat(checkConstraint).isPresent();
		assertThat(checkConstraint.get()).satisfies(constraint -> {
			assertThat(constraint.type()).isEqualTo(ConstraintType.CHECK);
			assertThat(constraint.definition()).isNotNull();
			assertThat(constraint.definition().toUpperCase()).contains("CONSTRAINT_POSITIVE_VALUE > 0");
		});
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/udt.sql")
	void extractsUserDefinedTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Optional<Udt> udtOptional = result.udts().stream()
				.filter(u -> "udt_status_enum".equalsIgnoreCase(u.name()))
				.findFirst();
		assertThat(udtOptional).isPresent();
		assertThat(udtOptional.get()).satisfies(udt -> {
			assertThat(udt.type()).isEqualTo("e");
		});
	}

	@TestTemplate
	@Databases({POSTGRES})
	@Scripts("de/yamass/redg/schema/sql/udt-structured.sql")
	void extractsStructuredUserDefinedTypes() throws SQLException {
		SchemaInspectionResult result = inspectPublicSchema();

		Optional<Udt> udtOptional = result.udts().stream()
				.filter(u -> "my_udt".equalsIgnoreCase(u.name()))
				.findFirst();
		assertThat(udtOptional.get()).satisfies(udt -> {
			assertThat(udt.type()).isEqualTo("c");
			assertThat(udt.fields()).hasSize(2);
			assertThat(udt.fields()).first().satisfies(field -> {
				assertThat(field.name()).isEqualToIgnoringCase("udt_int_column");
				assertThat(field.type().getName()).isIn("integer", "int4");
				assertThat(field.type().getJdbcType()).contains(java.sql.JDBCType.INTEGER);
			});
			assertThat(udt.fields()).element(1).satisfies(field -> {
						assertThat(field.name()).isEqualToIgnoringCase("udt_text_column");
						assertThat(field.type().getName()).isIn("text");
						assertThat(field.type().getJdbcType()).contains(java.sql.JDBCType.VARCHAR);
					}
			);
		});
	}

	private SchemaInspectionResult inspectPublicSchema() throws SQLException {
		SchemaInspector schemaInspector = new SchemaInspector(databaseType, dataSource);
		return schemaInspector.inspectSchema("public");
	}
}