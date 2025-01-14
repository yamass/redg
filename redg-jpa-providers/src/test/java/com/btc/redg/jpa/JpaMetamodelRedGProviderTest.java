/*
 * Copyright 2017 BTC Business Technology AG
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

package com.btc.redg.jpa;

import java.sql.Statement;
import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.btc.redg.generator.extractor.DatabaseManager;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import us.fatehi.utility.datasource.DatabaseConnectionSource;

/**
 * @author Yann Massard (yamass@gmail.com)
 */
public class JpaMetamodelRedGProviderTest {

	private static JpaMetamodelRedGProvider provider;
	private static Catalog catalog;

	@BeforeClass
	public static void setUp() throws Exception {
		provider = JpaMetamodelRedGProvider.fromPersistenceUnit("com.btc.redg");
		try(DatabaseConnectionSource dcs = DatabaseManager.createConnectionSource("jdbc:h2:mem:jpaprovidertest", "sa", "");
			Statement statement = dcs.get().createStatement()) {
			statement.execute("create table NON_MAPPED_TABLE ("
					+ "  NORMAL_COLUMN NUMBER(19),"
					+ "  FK NUMBER(19) references MANAGEDSUPERCLASSJOINED(ID),"
					+ ")");
			catalog = DatabaseManager.crawlDatabase(dcs, null, null);
		}
	}

	/**
	 * TODO explicit support (no fallback) for:
	 * secondary tables
	 * persistent sets
	 */

	@Test
	public void testGetClassNameForTable() throws Exception {
		Assert.assertEquals("ManagedSuperClassJoined", provider.getClassNameForTable(getTable("MANAGEDSUPERCLASSJOINED")));
		Assert.assertEquals("SubEntityJoined1", provider.getClassNameForTable(getTable("SUB_ENTITY_JOINED_1")));
		Assert.assertEquals("SubEntityJoined2", provider.getClassNameForTable(getTable("SUBENTITY_JOINED_2")));
		Assert.assertEquals("ManagedSuperClassSingleTable", provider.getClassNameForTable(getTable("MANAGED_SUPERCLASS_SINGLE_TABLE")));
		Assert.assertEquals("SubEntityTablePerClass1", provider.getClassNameForTable(getTable("SUBENTITYTABLEPERCLASS1")));
		Assert.assertEquals("SubEntityTablePerClass2", provider.getClassNameForTable(getTable("SUBENTITY_TABLE_PER_CLASS_2")));
	}

	@Test
	public void testGetMethodNameForColumnJoined() throws Exception {
		Assert.assertEquals("superJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGEDSUPERCLASSJOINED", "SUPERJOINEDIMPLICITENAMECOLUMN")));
		Assert.assertEquals("superJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGEDSUPERCLASSJOINED", "SUPER_JOINED_EXPLICIT_NAME_COLUMN")));

		Assert.assertEquals("subJoinedImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUB_ENTITY_JOINED_1", "SUBJOINEDIMPLICITENAMECOLUMN")));
		Assert.assertEquals("subJoinedExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUB_ENTITY_JOINED_1", "SUB_JOINED_EXPLICITE_NAME_COLUMN")));

		Assert.assertEquals("subEntityJoined2Attribute", provider.getMethodNameForColumn(getColumn("SUBENTITY_JOINED_2", "SUBENTITYJOINED2ATTRIBUTE")));
	}

	@Test
	public void testGetMethodNameForColumnSingleTable() throws Exception {
		Assert.assertEquals("superSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUPERSINGLETABLEIMPLICITENAMECOLUMN")));
		Assert.assertEquals("superSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUPER_SINGLE_TABLE_EXPLICIT_NAME_COLUMN")));

		Assert.assertEquals("subSingleTableImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUBSINGLETABLEIMPLICITENAMECOLUMN")));
		Assert.assertEquals("subSingleTableExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUB_SINGLE_TABLE_EXPLICITE_NAME_COLUMN")));

		Assert.assertEquals("subEntitySingleTable2Attribute", provider.getMethodNameForColumn(getColumn("MANAGED_SUPERCLASS_SINGLE_TABLE", "SUBENTITYSINGLETABLE2ATTRIBUTE")));
	}

	@Test
	public void testGetMethodNameForColumnTablePerClass() throws Exception {
		Assert.assertEquals("superTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUPERTABLEPERCLASSIMPLICITENAMECOLUMN")));
		Assert.assertEquals("superTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUPER_TABLE_PER_CLASS_EXPLICIT_NAME_COLUMN")));

		Assert.assertEquals("subTablePerClassImpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUBTABLEPERCLASSIMPLICITENAMECOLUMN")));
		Assert.assertEquals("subTablePerClassExpliciteNameColumn", provider.getMethodNameForColumn(getColumn("SUBENTITYTABLEPERCLASS1", "SUB_TABLE_PER_CLASS_EXPLICITE_NAME_COLUMN")));

		Assert.assertEquals("subEntityTablePerClass2Attribute", provider.getMethodNameForColumn(getColumn("SUBENTITY_TABLE_PER_CLASS_2", "SUBENTITYTABLEPERCLASS2ATTRIBUTE")));
	}

	@Test
	public void testGetMethodNameForForeignKey() throws Exception {
		Assert.assertEquals("subJoinedManyToOne", provider.getMethodNameForReference(getForeignKey("SUB_ENTITY_JOINED_1", "SUBJOINEDMANYTOONE_ID")));
		Assert.assertEquals("refEntity2", provider.getMethodNameForReference(getForeignKey("REFERENCEDENTITY1", "REFENTITY2_ID1")));
	}

	@Test
	public void testGetDataType() throws Exception {
			Assert.assertEquals("java.lang.Long", provider.getCanonicalDataTypeName(getColumn("REF_ENTITY_3", "ID")));
			Assert.assertEquals("long", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "ID")));

			Assert.assertEquals("java.lang.Integer", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "SUBENTITY_ID")));
	}

	@Test
	public void testGetDataTypeForEmbedded() throws Exception {
			Assert.assertEquals("long", provider.getCanonicalDataTypeName(getColumn("REFERENCEDENTITY1", "EMBEDDEDLONGATTRIBUTE")));
	}

	@Test
	public void testGetMethodNameForForeignKeyColumn() throws Exception {
		Assert.assertEquals("refEntity2Id2", provider.getMethodNameForForeignKeyColumn(
				getForeignKey("REFERENCEDENTITY1", "REFENTITY2_ID_2"),
				getColumn("REF_ENTITY_2", "ID_2"),
				getColumn("REFERENCEDENTITY1", "REFENTITY2_ID_2"))
		);
		Assert.assertEquals("referencedEntity2WithExpliciteJoinColumnsId2", provider.getMethodNameForForeignKeyColumn(
				getForeignKey("REFERENCEDENTITY1", "REF_2_ID2"),
				getColumn("REF_ENTITY_2", "ID_2"),
				getColumn("REFERENCEDENTITY1", "REF_2_ID2"))
		);
	}

	@Test
	public void testFallBackToDefaultImplementation() throws Exception {
		Assert.assertEquals("NonMappedTable", provider.getClassNameForTable(getTable("NON_MAPPED_TABLE")));
		Assert.assertEquals("normalColumn", provider.getMethodNameForColumn(getColumn("NON_MAPPED_TABLE", "NORMAL_COLUMN")));
		Assert.assertEquals("fkManagedsuperclassjoined", provider.getMethodNameForReference(getForeignKey("NON_MAPPED_TABLE", "FK")));
		Assert.assertEquals("fkManagedsuperclassjoinedId", provider.getMethodNameForForeignKeyColumn(getForeignKey("NON_MAPPED_TABLE", "FK"), getColumn("MANAGEDSUPERCLASSJOINED", "ID"), getColumn("NON_MAPPED_TABLE", "FK")));
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