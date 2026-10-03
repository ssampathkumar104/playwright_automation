package dataUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
*
* Represents a CSV file containing tabular data and provides methods to read the data from the file.
*/
public class CSVTabularDataFile {
	private final String path;

	private final String delimiter;

	/**
	 * Creates a new instance of CSVTabularDataFile with the specified file path and delimiter.
	 *
	 * @param path      the path to the CSV file
	 * @param delimiter the delimiter used to separate values in the CSV file
	 * 
	 * @return CSVTabularDataFile class Object
	 */
	public static CSVTabularDataFile from(String path, String delimiter) {
		return new CSVTabularDataFile(path, delimiter);
	}

	/**
	 * Constructor with path and delimiter.
	 */
	private CSVTabularDataFile(String path, String delimiter) {
		this.path = new File(path).toString();
		this.delimiter = delimiter;
	}

	/**
	 * Reads the CSV file and returns the tabular data as a CSVTabularData object.
	 *
	 * @return the CSVTabularData object representing the data in the file
	 */
	public CSVTabularData getTable() {
		CSVTabularData td = new CSVTabularData();
		try {
			FileInputStream fstream = new FileInputStream(this.path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] splitted = strLine.split(Pattern.quote(this.delimiter));
				td.addRow(splitted);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return td;
	}
}
