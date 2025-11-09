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

package de.yamass.redg.jpa;

import de.yamass.redg.generator.RedGGenerator;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.SchemaInspectionResult;
import de.yamass.redg.schema.model.Table;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author Yann Massard (yamass@gmail.com)
 */
class JpaMetamodelRedGProviderTest {

	private static JpaMetamodelRedGProvider provider;
	private static SchemaInspectionResult schemaResult;

	@BeforeAll
	public static void setUp() throws Exception {
		provider = JpaMetamodelRedGProvider.fromPersistenceUnit("de.yamass.redg");
		DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:jpaprovidertest", "sa", "");
		try(Connection connection = dataSource.getConnection();
			Statement statement = connection.createStatement()) {
			statement.execute("create table NON_MAPPED_TABLE ("
					+ "  NORMAL_COLUMN NUMBER(19),"
					+ "  FK NUMBER(19) references MANAGEDSUPERCLASSJOINED(ID)"
					+ ")");
			schemaResult = RedGGenerator.inspectSchemas(dataSource, null);
		}
	}

	/**
	 * TODO explicit support (no fallback) for:
	 * secondary tables
	 * persistent sets
	 */

	@Test
	void testGetClassNameForTable() throws Exception {
		Assertions.assertEquals("ManagedSuperClassJoined", provider.getClassNameForTable(getTable("MANAGEDSUPERCLASSJOINED")));
		Assertions.assertEquals("SubEntityJoined1", provider.getClassNameForTable(getTable("SUB_ENTITY_JOINED_1")));
		Assertions.assertEquals("SubEntityJoined2", provider.getClassNameForTable(getTable("SUBENTITY_JOINED_2")));
		Assertions.assertEquals("ManagedSuperClassSingleTable", provider.getClassNameForTable(getTable("MANAGED_SUPERCLASS_SINGLE_TABLE")));
		Assertions.assertEquals("SubEntityTablePerClass1", provider.getClassNameForTable(getTable("SUBENTITYTABLEPERCLASS1")));
		Assertions.assertEquals("SubEntityTablePerClass2", provider.getClassNameForTable(getTable("SUBENTITY_TABLE_PER_CLASS_2")));
	}

	@Test
	void testGetMethodNameForColumnJoined() throws Exception {
		Table table1 = getTable("MANAGEDSUPERCLASSJOINED");
		Assertions.assertEquals("superJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUPERJOINEDIMPLICITENAMECOLUMN"), table1));
		Assertions.assertEquals("superJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUPER_JOINED_EXPLICIT_NAME_COLUMN"), table1));

		Table table2 = getTable("SUB_ENTITY_JOINED_1");
		Assertions.assertEquals("subJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table2, "SUBJOINEDIMPLICITENAMECOLUMN"), table2));
		Assertions.assertEquals("subJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table2, "SUB_JOINED_EXPLICITE_NAME_COLUMN"), table2));

		Table table3 = getTable("SUBENTITY_JOINED_2");
		Assertions.assertEquals("subEntityJoined2Attribute", provider.getMethodNameForColumn(getColumn(table3, "SUBENTITYJOINED2ATTRIBUTE"), table3));
	}

	@Test
	void testGetMethodNameForColumnSingleTable() throws Exception {
		Table table = getTable("MANAGED_SUPERCLASS_SINGLE_TABLE");
		Assertions.assertEquals("superSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table, "SUPERSINGLETABLEIMPLICITENAMECOLUMN"), table));
		Assertions.assertEquals("superSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table, "SUPER_SINGLE_TABLE_EXPLICIT_NAME_COLUMN"), table));

		Assertions.assertEquals("subSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table, "SUBSINGLETABLEIMPLICITENAMECOLUMN"), table));
		Assertions.assertEquals("subSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table, "SUB_SINGLE_TABLE_EXPLICITE_NAME_COLUMN"), table));

		Assertions.assertEquals("subEntitySingleTable2Attribute", provider.getMethodNameForColumn(getColumn(table, "SUBENTITYSINGLETABLE2ATTRIBUTE"), table));
	}

	@Test
	void testGetMethodNameForColumnTablePerClass() throws Exception {
		Table table1 = getTable("SUBENTITYTABLEPERCLASS1");
		Assertions.assertEquals("superTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUPERTABLEPERCLASSIMPLICITENAMECOLUMN"), table1));
		Assertions.assertEquals("superTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUPER_TABLE_PER_CLASS_EXPLICIT_NAME_COLUMN"), table1));

		Assertions.assertEquals("subTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUBTABLEPERCLASSIMPLICITENAMECOLUMN"), table1));
		Assertions.assertEquals("subTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn(table1, "SUB_TABLE_PER_CLASS_EXPLICITE_NAME_COLUMN"), table1));

		Table table2 = getTable("SUBENTITY_TABLE_PER_CLASS_2");
		Assertions.assertEquals("subEntityTablePerClass2Attribute", provider.getMethodNameForColumn(getColumn(table2, "SUBENTITYTABLEPERCLASS2ATTRIBUTE"), table2));
	}

	@Test
	void testGetMethodNameForForeignKey() throws Exception {
		Table table1 = getTable("SUB_ENTITY_JOINED_1");
		Assertions.assertEquals("subJoinedManyToOne", provider.getMethodNameForReference(getForeignKey(table1, "SUBJOINEDMANYTOONE_ID")));
		
		Table table2 = getTable("REFERENCEDENTITY1");
		Assertions.assertEquals("refEntity2", provider.getMethodNameForReference(getForeignKey(table2, "REFENTITY2_ID1")));
	}

	@Test
	void testGetDataType() throws Exception {
		Table table1 = getTable("REF_ENTITY_3");
		Assertions.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(getColumn(table1, "ID"), table1));
		
		Table table2 = getTable("REFERENCEDENTITY1");
		Assertions.assertEquals("long", provider.getCanonicalDataTypeName(getColumn(table2, "ID"), table2));
		Assertions.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(getColumn(table2, "SUBENTITY_ID"), table2));
	}

	@Test
	void testGetDataTypeForEmbedded() throws Exception {
		Table table = getTable("REFERENCEDENTITY1");
		Assertions.assertEquals("long", provider.getCanonicalDataTypeName(getColumn(table, "EMBEDDEDLONGATTRIBUTE"), table));
	}

	@Test
	void testGetMethodNameForForeignKeyColumn() throws Exception {
		Table sourceTable = getTable("REFERENCEDENTITY1");
		ForeignKey fk1 = getForeignKey(sourceTable, "REFENTITY2_ID_2");
		ForeignKeyColumn fkCol1 = fk1.columns().stream()
				.filter(fkCol -> fkCol.sourceColumn().name().equals("REFENTITY2_ID_2"))
				.findFirst().orElseThrow();
		Assertions.assertEquals("refEntity2Id2", provider.getMethodNameForForeignKeyColumn(fkCol1, sourceTable));
		
		ForeignKey fk2 = getForeignKey(sourceTable, "REF_2_ID2");
		ForeignKeyColumn fkCol2 = fk2.columns().stream()
				.filter(fkCol -> fkCol.sourceColumn().name().equals("REF_2_ID2"))
				.findFirst().orElseThrow();
		Assertions.assertEquals("referencedEntity2WithExpliciteJoinColumnsId2", provider.getMethodNameForForeignKeyColumn(fkCol2, sourceTable));
	}

	@Test
	void testFallBackToDefaultImplementation() throws Exception {
		Table table = getTable("NON_MAPPED_TABLE");
		Assertions.assertEquals("NonMappedTable", provider.getClassNameForTable(table));
		Assertions.assertEquals("normalColumn", provider.getMethodNameForColumn(getColumn(table, "NORMAL_COLUMN"), table));
		Assertions.assertEquals("fkManagedsuperclassjoined", provider.getMethodNameForReference(getForeignKey(table, "FK")));
		
		ForeignKey fk = getForeignKey(table, "FK");
		ForeignKeyColumn fkCol = fk.columns().stream()
				.filter(fkColumn -> fkColumn.sourceColumn().name().equals("FK"))
				.findFirst().orElseThrow();
		Assertions.assertEquals("fkManagedsuperclassjoinedId", provider.getMethodNameForForeignKeyColumn(fkCol, table));
	}

	private Column getColumn(Table table, String columnName) {
		return table.findColumn(columnName).orElse(null);
	}

	private Table getTable(String tableName) {
		return schemaResult.tables().stream()
				.filter(t -> t.name().equalsIgnoreCase(tableName))
				.findFirst().orElse(null);
	}

	private ForeignKey getForeignKey(Table referencingTable, String fkColumnName) {
		return referencingTable.outgoingForeignKeys().stream()
				.filter(foreignKey -> foreignKey.columns().stream()
						.anyMatch(fkCol -> fkCol.sourceColumn().name().equalsIgnoreCase(fkColumnName)))
				.findFirst().orElse(null);
	}
}