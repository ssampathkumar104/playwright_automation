package dataUtils.ExcelData;

import java.util.ArrayList;
import java.util.List;

/**
 * The ExcelData class represents a collection of Excel sheets.
 */
public class ExcelData {
	List<ExcelSheet> excel;

	/**
	 * Constructs an ExcelData object with an empty list of Excel sheets.
	 */
	public ExcelData() {
		this.excel = new ArrayList<ExcelSheet>();
	}

	/**
	 * Returns the TableData object for the specified sheet index.
	 *
	 * @param sheetIndex the index of the sheet
	 * @return the TableData object for the specified sheet index
	 */
	public TableData sheet(final int sheetIndex) {
		return this.excel.get(sheetIndex).getTable();
	}

	/**
	 * Returns the TableData object for the specified sheet name.
	 *
	 * @param sheetName the name of the sheet
	 * @return the TableData object for the specified sheet name
	 */
	public TableData sheet(final String sheetName) {
		if (sheetName != null) {
			for (final ExcelSheet es : this.excel) {
				if (sheetName.equals(es.getSheetName())) {
					return es.getTable();
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the ExcelData object is empty.
	 *
	 * @return true if the ExcelData object is empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.excel.isEmpty();
	}

	/**
	 * Returns the number of sheets in the ExcelData object.
	 *
	 * @return the number of sheets in the ExcelData object
	 */
	public int sheetCount() {
		return this.excel.size();
	}

	/**
	 * Clears the ExcelData object by removing all sheets.
	 */
	public void clear() {
		this.excel.clear();
	}

	/**
	 * Adds a new sheet to the ExcelData object with the specified TableData content.
	 *
	 * @param tabContent the TableData content of the sheet
	 */
	public void add(final TableData tabContent) {
		this.excel.add(new ExcelSheet("", tabContent));
	}

	/**
	 * Adds a new sheet to the ExcelData object with the specified TableData content and sheet name.
	 *
	 * @param tabContent the TableData content of the sheet
	 * @param sheetName  the name of the sheet
	 */
	public void add(final TableData tabContent, final String sheetName) {
		this.excel.add(new ExcelSheet("", tabContent));
	}

	/**
	 * Adds an ExcelSheet object to the ExcelData object.
	 *
	 * @param sheet the ExcelSheet object to be added
	 */
	public void add(final ExcelSheet sheet) {
		this.excel.add(sheet);
	}

	/**
	 * Removes the sheet at the specified tab index.
	 *
	 * @param tabIndex the index of the sheet to be removed
	 */
	public void remove(final int tabIndex) {
		this.excel.remove(tabIndex);
	}

	/**
	 * Returns a string representation of the ExcelData object.
	 *
	 * @return a string representation of the ExcelData object
	 */
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		for (final ExcelSheet sheet : this.excel) {
			sb.append(sheet.table.toString()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Retrieves the list of ExcelSheet objects in the ExcelData object.
	 *
	 * @return the list of ExcelSheet objects
	 */
	public List<ExcelSheet> getExcel() {
		return this.excel;
	}
}
