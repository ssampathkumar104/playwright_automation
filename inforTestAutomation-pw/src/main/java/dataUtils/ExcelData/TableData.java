package dataUtils.ExcelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TableData represents a table of data in an Excel sheet.
 */
public class TableData {
	List<List<String>> table;

	/**
	 * Constructs an empty TableData object.
	 */
	public TableData() {
		this.table = new ArrayList<List<String>>();
	}

	/**
	 * Checks if the table contains a specific value.
	 *
	 * @param toSearch the value to search for
	 * @return true if the value is found, false otherwise
	 */
	public boolean contains(final String toSearch) {
		for (final List<String> row : this.table) {
			if (row.contains(toSearch)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the value at the specified row and column indices.
	 *
	 * @param rowIndex    the index of the row
	 * @param columnIndex the index of the column
	 * @return the value at the specified indices, or null if out of bounds
	 */
	public String get(final int rowIndex, final int columnIndex) {
		try {
			return this.table.get(rowIndex).get(columnIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Retrieves the index of a column with the specified column name.
	 *
	 * @param columnName the name of the column
	 * @return the index of the column, or -1 if not found
	 */
	public int getColumnIndex(final String columnName) {
		return this.row(0).indexOf(columnName);
	}

	/**
	 * Checks if the table is empty.
	 *
	 * @return true if the table is empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.table.isEmpty();
	}

	/**
	 * Retrieves the number of rows in the table.
	 *
	 * @return the number of rows
	 */
	public int rowCount() {
		return this.table.size();
	}

	/**
	 * Retrieves the number of columns in the table.
	 *
	 * @return the number of columns
	 */
	public int columnCount() {
		if (this.table.size() > 0) {
			return this.table.get(0).size();
		}
		return 0;
	}

	/**
	 * Clears the table, removing all rows and columns.
	 */
	public void clear() {
		this.table.clear();
	}

	/**
	 * Sets the value at the specified row and column indices.
	 * If the row or column does not exist, it is automatically created.
	 *
	 * @param rowIndex    the index of the row
	 * @param columnIndex the index of the column
	 * @param value       the value to set
	 */
	public void set(final int rowIndex, final int columnIndex, final String value) {
		if (this.table.size() - 1 < rowIndex) {
			for (int i = this.table.size(); i < rowIndex + 1; ++i) {
				final List<String> newRow = new ArrayList<String>();
				this.table.add(newRow);
			}
		}
		final List<String> row = this.table.get(rowIndex);
		if (row.size() - 1 < columnIndex) {
			for (int j = row.size(); j < columnIndex + 1; ++j) {
				row.add(null);
			}
		}
		row.set(columnIndex, value);
	}

	/**
	 * Removes the value at the specified row and column indices, setting it to null.
	 *
	 * @param rowIndex    the index of the row
	 * @param columnIndex the index of the column
	 */
	public void remove(final int rowIndex, final int columnIndex) {
		this.table.get(rowIndex).set(columnIndex, null);
	}

	/**
	 * Retrieves a specific row in the table.
	 *
	 * @param rowIndex the index of the row
	 * @return the row as a list of strings
	 */
	public List<String> row(final int rowIndex) {
		return this.table.get(rowIndex);
	}

	/**
	 * Retrieves a specific column in the table.
	 *
	 * @param columnIndex the index of the column
	 * @return the column as a list of strings
	 */
	public List<String> column(final int columnIndex) {
		final List<String> column = new ArrayList<String>();
		for (final List<String> row : this.table) {
			column.add(row.get(columnIndex));
		}
		return column;
	}

	/**
	 * Adds a new row to the table using the specified array of values.
	 *
	 * @param row the values to add as a row
	 */
	public void addRow(final String[] row) {
		this.table.add(Arrays.asList(row));
	}

	/**
	 * Compares the TableData object with another object for equality.
	 *
	 * @param obj the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final TableData other = (TableData) obj;
		return other.rowCount() == this.rowCount() && other.toString().equals(this.toString());
	}
}