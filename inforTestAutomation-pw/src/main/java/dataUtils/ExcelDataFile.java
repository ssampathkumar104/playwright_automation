package dataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import dataUtils.ExcelData.ExcelData;
import dataUtils.ExcelData.ExcelSheet;
import dataUtils.ExcelData.TableData;

/**
*
* Represents an Excel file and provides methods to read data from it.
*/
public class ExcelDataFile {
	private final String path;
	private Workbook workbook;
	
	/**
	 * Creates a new instance of ExcelDataFile with the specified file path.
	 *
	 * @param path the path to the Excel file
	 */
	private ExcelDataFile(final String path) {
		this.path = new File(path).toString();
	}

	/**
	 * Reads the Excel file and returns the ExcelData object representing the data in the file.
	 *
	 * @param path the path to the Excel file
	 * @return the ExcelData object representing the data in the file
	 */
	public static ExcelDataFile readExcelFile(final String path) {
		return new ExcelDataFile(path);
	}

	/**
	 * Gets the ExcelData object representing the data in the Excel file.
	 *
	 * @return the ExcelData object representing the data in the file
	 */
	public ExcelData getExcel() {
		this.loadWorkbook();
		final ExcelData edf = new ExcelData();
		try {
			for (int sheetCount = this.workbook.getNumberOfSheets(), i = 0; i < sheetCount; ++i) {
				final TableData table = new TableData();
				final Sheet sheet = this.workbook.getSheetAt(i);
				final Iterator<Row> rowIterator = sheet.iterator();
				int rowId = 0;
				while (rowIterator.hasNext()) {
					final Row row = rowIterator.next();
					final Iterator<Cell> cellIterator = row.cellIterator();
					int columnId = 0;
					while (cellIterator.hasNext()) {
						final Cell cell = cellIterator.next();
						cell.setCellType(CellType.STRING);
						columnId = cell.getColumnIndex();
						table.set(rowId, columnId, cell.getStringCellValue());
					}
					++rowId;
				}
				edf.add(new ExcelSheet(sheet.getSheetName(), table));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return edf;
	}

	/**
	 * Gets the data from the Excel file as a map, where the keys are values from the first column and the values are values from the second column.
	 *
	 * @return a map representing the data in the Excel file
	 */
	public Map<String, String> getExcelDataMap() {
		final ExcelDataFile tbFile = new ExcelDataFile(this.path);
		final ExcelData excel = tbFile.getExcel();
		final Map<String, String> excelDatamap = new LinkedHashMap<>();
		for (int i = 0; i < excel.sheetCount(); ++i) {
			for (int x = 0; x < excel.sheet(i).rowCount(); ++x) {
				final String key = excel.sheet(i).get(x, 0);
				final String value = excel.sheet(i).get(x, 1);
				excelDatamap.put(key, value);
			}
		}
		return excelDatamap;
	}

	/**
	 * Loads the workbook from the Excel file.
	 */
	private void loadWorkbook() {
		this.workbook = getWorkbook(this.path);
	}

	/**
	 * Reads the workbook from the Excel file.
	 *
	 * @param path the path to the Excel file
	 * @return the workbook object representing the Excel file
	 */
	public static Workbook getWorkbook(final String path) {
		try (final FileInputStream file = new FileInputStream(new File(path))) {
			return WorkbookFactory.create(file);
		} catch (IOException ex2) {
			final Exception ex = null;
			final Exception e = ex;
			throw new RuntimeException(e);
		}
	}

}