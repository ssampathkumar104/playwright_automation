package dataUtils.ExcelData;

/**
 * ExcelSheet represents a single sheet within an Excel file.
 */
public class ExcelSheet {
	
	//Global variables
	public String sheetName;
	TableData table;

	/**
	 * Retrieves the name of the Excel sheet.
	 *
	 * @return the sheet name
	 */
	public String getSheetName() {
		return this.sheetName;
	}

	/**
	 * Sets the name of the Excel sheet.
	 *
	 * @param sheetName the sheet name to set
	 */
	public void setSheetName(final String sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * Retrieves the table data associated with the Excel sheet.
	 *
	 * @return the table data
	 */
	public TableData getTable() {
		return this.table;
	}

	/**
	 * Sets the table data for the Excel sheet.
	 *
	 * @param table the table data to set
	 */
	public void setTable(final TableData table) {
		this.table = table;
	}

	/**
	 * Constructs a new ExcelSheet object with the specified sheet name and table data.
	 *
	 * @param sheetName the name of the sheet
	 * @param table the table data
	 */
	public ExcelSheet(final String sheetName, final TableData table) {
		this.sheetName = sheetName;
		this.table = table;
	}
}
