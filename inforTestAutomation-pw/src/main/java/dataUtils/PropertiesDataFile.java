package dataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
*
* Represents a properties data file and provides methods to retrieve values based on keys.
*/
public class PropertiesDataFile {

	private File file;

	/**
	 * Creates a new instance of PropertiesDataFile with the specified file.
	 *
	 * @param file the properties file
	 */
	public PropertiesDataFile(File file) {
		this.file = file;
	}

	static Properties prop = new Properties();
	
	/**
	 * Retrieves the value associated with the specified key from the properties file.
	 *
	 * @param key the key to look up
	 * @return the value associated with the key, or null if the key is not found
	 */
	public String get(String key) {
		try (final FileInputStream fis = new FileInputStream(file)) {
			prop.load(fis);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return prop.getProperty(key);
	}

}