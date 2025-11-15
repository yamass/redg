package de.yamass.redg.schema.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable implementation of Table that allows adding foreign keys after instantiation.
 * This is necessary because Table and ForeignKey have circular references - a Table contains
 * ForeignKeys, and ForeignKeys reference Tables. Using a mutable implementation allows us
 * to break the circular dependency during construction.
 */
public final class MutableTable implements Table {
	private final String schemaName;
	private final String name;
	private final List<Column> columns;
	private final List<Column> primaryKeyColumns;
	private final List<ForeignKey> outgoingForeignKeys;
	private final List<ForeignKey> incomingForeignKeys;

	public MutableTable(String schemaName, String name) {
		this.schemaName = schemaName;
		this.name = name;
		this.columns = new ArrayList<>();
		this.primaryKeyColumns = new ArrayList<>();
		this.outgoingForeignKeys = new ArrayList<>();
		this.incomingForeignKeys = new ArrayList<>();
	}

	@Override
	public String schemaName() {
		return schemaName;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<Column> columns() {
		return columns;
	}

	@Override
	public List<Column> primaryKeyColumns() {
		return primaryKeyColumns;
	}

	@Override
	public List<ForeignKey> outgoingForeignKeys() {
		return outgoingForeignKeys;
	}

	@Override
	public List<ForeignKey> incomingForeignKeys() {
		return incomingForeignKeys;
	}

	public void addOutgoingForeignKey(ForeignKey foreignKey) {
		outgoingForeignKeys.add(foreignKey);
	}

	public void addIncomingForeignKey(ForeignKey foreignKey) {
		incomingForeignKeys.add(foreignKey);
	}

	/**
	 * Adds a column to this table.
	 * This method allows adding columns after table instantiation, similar to how foreign keys are added.
	 *
	 * @param column The column to add
	 */
	public void addColumn(Column column) {
		columns.add(column);
	}

	/**
	 * Adds a column to the primary key columns list.
	 *
	 * @param column The primary key column to add
	 */
	public void addPrimaryKeyColumn(Column column) {
		primaryKeyColumns.add(column);
	}
}

