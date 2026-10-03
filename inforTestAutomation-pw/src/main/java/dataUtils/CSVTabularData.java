package dataUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.repackaged.com.google.common.base.Joiner;

/**
*
* Represents tabular data in CSV format.
*/
public class CSVTabularData {
	List<List<String>> table;

	/**
	 * Constructor an empty CSVTabularData object.
	 */
	public CSVTabularData() {
		this.table = new ArrayList<>();
	}

	/**
	 * Adds a new row to the CSVTabularData.
	 *
	 * @param row the array of strings representing the row data
	 */
	public void addRow(String[] row) {
		this.table.add(Arrays.asList(row));
	}

	/**
	 * Constructs a CSVTabularData object from a list of string arrays.
	 *
	 * @param table the list of string arrays representing the table data
	 */
	public CSVTabularData(List<String[]> table) {
		this.table = new ArrayList<>();
		for (String[] row : table)
			addRow(row);
	}

	/**
	 * Retrieves the value at the specified row and column index.
	 *
	 * @param rowIndex    the index of the row
	 * @param columnIndex the index of the column
	 * @return the value at the specified row and column
	 */
	public String get(int rowIndex, int columnIndex) {
		try {
			return (this.table.get(rowIndex)).get(columnIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Checks if the CSVTabularData is empty.
	 *
	 * @return true if the table is empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.table.isEmpty();
	}

	/**
	 * Retrieves the value at the specified row number and column name.
	 *
	 * @param columnName the name of the column
	 * @param rowNumber  the number of the row
	 * @return the value at the specified row and column
	 * @throws Exception if the column name is invalid
	 */
	public String get(String columnName, int rowNumber) throws Exception {
		int columnIndex = getColumnIndex(columnName);
		if (columnIndex > -1)
			return get(rowNumber, columnIndex);
		throw new Exception("Invalid Column Name");
	}

	/**
	 * Retrieves the index of the specified column name.
	 *
	 * @param columnName the name of the column
	 * @return the index of the column, or -1 if not found
	 */
	public int getColumnIndex(String columnName) {
		return row(0).indexOf(columnName);
	}

	/**
	 * Retrieves the values in the specified column.
	 *
	 * @param columnIndex the index of the column
	 * @return the list of values in the column
	 */
	public List<String> column(int columnIndex) {
		List<String> column = new ArrayList<>();
		for (List<String> row : this.table)
			column.add(row.get(columnIndex));
		return column;
	}

	/**
	 * Retrieves the values in the specified column by column name.
	 *
	 * @param columnName the name of the column
	 * @return the list of values in the column
	 * @throws Exception if the column name is invalid
	 */
	public List<String> column(String columnName) throws Exception {
		int columnIndex = getColumnIndex(columnName);
		if (columnIndex > -1) {
			List<String> column = column(columnIndex);
			column.remove(0);
			return column;
		}
		throw new Exception("Invalid Column Name");
	}

	/**
	 * Retrieves the values in the specified row.
	 *
	 * @param rowIndex the index of the row
	 * @return the list of values in the row
	 */
	public List<String> row(int rowIndex) {
		return this.table.get(rowIndex);
	}

	/**
	 * Retrieves the number of rows in the CSVTabularData.
	 *
	 * @return the number of rows
	 */
	public int rowCount() {
		return this.table.size();
	}

	/**
	 * Retrieves the number of columns in the CSVTabularData.
	 *
	 * @return the number of columns
	 */
	public int columnCount() {
		if (!this.table.isEmpty())
			return ((List<?>) this.table.get(0)).size();
		return 0;
	}

	/**
	 * Checks if the CSVTabularData contains the specified value.
	 *
	 * @param toSearch the value to search for
	 * @return true if the value is found, false otherwise
	 */
	public boolean contains(String toSearch) {
		for (List<String> row : this.table) {
			if (row.contains(toSearch))
				return true;
		}
		return false;
	}

	/**
	 * Clears the CSVTabularData, removing all rows.
	 */
	public void clear() {
		this.table.clear();
	}

	/**
	 * Sets the value at the specified row and column index.
	 *
	 * @param rowIndex    the index of the row
	 * @param columnIndex the index of the column
	 * @param value       the value to set
	 */
	public void set(int rowIndex, int columnIndex, String value) {
		if (this.table.size() - 1 < rowIndex)
			for (int i = this.table.size(); i < rowIndex + 1; i++) {
				List<String> newRow = new ArrayList<>();
				this.table.add(newRow);
			}
		List<String> row = this.table.get(rowIndex);
		if (row.size() - 1 < columnIndex)
			for (int i = row.size(); i < columnIndex + 1; i++)
				row.add(null);
		row.set(columnIndex, value);
	}

	/**
	 * Converts the CSVTabularData to a CSV string representation.
	 *
	 * @return the CSV string
	 */
	public String toCsv() {
		StringBuffer sb = new StringBuffer();
		String newLine = "";
		for (List<String> row : this.table) {
			sb.append(newLine).append(Joiner.on(",").join(row));
			newLine = "\n";
		}
		return sb.toString();
	}

	/**
	 * Checks if the CSVTabularData is equal to another object.
	 *
	 * @param obj the object to compare
	 * @return true if the objects are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSVTabularData other = (CSVTabularData) obj;
		if (other.rowCount() != rowCount())
			return false;
		return other.toString().equals(toString());
	}

	/**
	 * Retrieves the string representation of the CSVTabularData.
	 *
	 * @return the string representation
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (List<String> row : this.table)
			sb.append(row.toString());
		return sb.toString();
	}
}
