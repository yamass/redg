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

import de.yamass.redg.generator.extractor.DatabaseManager;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

/**
 * @author Yann Massard (yamass@gmail.com)
 */
class JpaMetamodelRedGProviderTest {

	private static JpaMetamodelRedGProvider provider;
	private static Catalog catalog;

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
			catalog = DatabaseManager.crawlDatabase(dataSource, null, null);
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
		Assertions.assertEquals("superJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGEDSUPERCLASSJOINED", "SUPERJOINEDIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("superJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGEDSUPERCLASSJOINED", "SUPER_JOINED_EXPLICIT_NAME_COLUMN")));

		Assertions.assertEquals("subJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUB_ENTITY_JOINED_1", "SUBJOINEDIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("subJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUB_ENTITY_JOINED_1", "SUB_JOINED_EXPLICITE_NAME_COLUMN")));

		Assertions.assertEquals("subEntityJoined2Attribute", provider.getMethodNameForColumn(getColumn("SUBENTITY_JOINED_2", "SUBENTITYJOINED2ATTRIBUTE")));
	}

	@Test
	void testGetMethodNameForColumnSingleTable() throws Exception {
		Assertions.assertEquals("superSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUPERSINGLETABLEIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("superSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUPER_SINGLE_TABLE_EXPLICIT_NAME_COLUMN")));

		Assertions.assertEquals("subSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUBSINGLETABLEIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("subSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUB_SINGLE_TABLE_EXPLICITE_NAME_COLUMN")));

		Assertions.assertEquals("subEntitySingleTable2Attribute", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUBENTITYSINGLETABLE2ATTRIBUTE")));
	}

	@Test
	void testGetMethodNameForColumnTablePerClass() throws Exception {
		Assertions.assertEquals("superTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUPERTABLEPERCLASSIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("superTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUPER_TABLE_PER_CLASS_EXPLICIT_NAME_COLUMN")));

		Assertions.assertEquals("subTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUBTABLEPERCLASSIMPLICITENAMECOLUMN")));
		Assertions.assertEquals("subTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUB_TABLE_PER_CLASS_EXPLICITE_NAME_COLUMN")));

		Assertions.assertEquals("subEntityTablePerClass2Attribute", provider.getMethodNameForColumn(getColumn("SUBENTITY_TABLE_PER_CLASS_2", "SUBENTITYTABLEPERCLASS2ATTRIBUTE")));
	}

	@Test
	void testGetMethodNameForForeignKey() throws Exception {
		Assertions.assertEquals("subJoinedManyToOne", provider.getMethodNameForReference(getForeignKey("SUB_ENTITY_JOINED_1", "SUBJOINEDMANYTOONE_ID")));
		Assertions.assertEquals("refEntity2", provider.getMethodNameForReference(getForeignKey("REFERENCEDENTITY1", "REFENTITY2_ID1")));
	}

	@Test
	void testGetDataType() throws Exception {
			Assertions.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(getColumn("REF_ENTITY_3", "ID")));
			Assertions.assertEquals("long", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "ID")));

			Assertions.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "SUBENTITY_ID")));
	}

	@Test
	void testGetDataTypeForEmbedded() throws Exception {
			Assertions.assertEquals("long", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "EMBEDDEDLONGATTRIBUTE")));
	}

	@Test
	void testGetMethodNameForForeignKeyColumn() throws Exception {
		Assertions.assertEquals("refEntity2Id2", provider.getMethodNameForForeignKeyColumn(
				getForeignKey("REFERENCEDENTITY1", "REFENTITY2_ID_2"),
				getColumn("REF_ENTITY_2", "ID_2"),
				getColumn("REFERENCEDENTITY1", "REFENTITY2_ID_2")));
		Assertions.assertEquals("referencedEntity2WithExpliciteJoinColumnsId2", provider.getMethodNameForForeignKeyColumn(
				getForeignKey("REFERENCEDENTITY1", "REF_2_ID2"),
				getColumn("REF_ENTITY_2", "ID_2"),
				getColumn("REFERENCEDENTITY1", "REF_2_ID2")));
	}

	@Test
	void testFallBackToDefaultImplementation() throws Exception {
		Assertions.assertEquals("NonMappedTable", provider.getClassNameForTable(getTable("NON_MAPPED_TABLE")));
		Assertions.assertEquals("normalColumn", provider.getMethodNameForColumn(getColumn("NON_MAPPED_TABLE", "NORMAL_COLUMN")));
		Assertions.assertEquals("fkManagedsuperclassjoined", provider.getMethodNameForReference(getForeignKey("NON_MAPPED_TABLE", "FK")));
		Assertions.assertEquals("fkManagedsuperclassjoinedId", provider.getMethodNameForForeignKeyColumn(getForeignKey("NON_MAPPED_TABLE", "FK"), getColumn("MANAGEDSUPERCLASSJOINED", "ID"), getColumn("NON_MAPPED_TABLE", "FK")));
	}

	private Column getColumn(String tableName, String columnName) {
		Table table = getTable(tableName);
		if (table != null) {
			Optional<Column> column = table.getColumns().stream()
					.filter(c -> c.getName().equals(columnName))
					.findAny();
			if (column.isPresent()) {
				return column.get();
			}
		}
		return null;
	}

	private Table getTable(String tableName) {
		return catalog.getTables().stream()
                    .filter(t -> t.getName().equals(tableName))
                    .findFirst().orElse(null);
	}

	private ForeignKey getForeignKey(String referencingTableName, String fkColumnName) {
		return getTable(referencingTableName).getImportedForeignKeys().stream()
				.filter(foreignKeyColumnReferences ->  foreignKeyColumnReferences.getColumnReferences().stream()
						.anyMatch(foreignKeyColumnReference -> foreignKeyColumnReference.getForeignKeyColumn().getName().equals(fkColumnName)))
				.findAny().orElse(null);
	}
}