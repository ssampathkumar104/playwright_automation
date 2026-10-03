package dataUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Predicate;

import testBase.BaseClass;

/**
*
* Represents a JSON data file and provides methods to read and manipulate JSON data.
*/
public final class JsonDataFile {

	private File file;

	/**
	 * Creates a new instance of JsonDataFile with the specified file.
	 *
	 * @param file the JSON file
	 */
	public JsonDataFile(File file) {
		this.file = file;
	}

	/**
	 * Reads the content of the JSON file.
	 *
	 * @return the content of the JSON file as a string
	 */
	public String getContent() {
		String content = null;
		try {
			content = FileUtils.readFileToString(this.file, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Parses the JSON file and returns the JSON object.
	 *
	 * @return the JSON object representing the data in the file
	 */
	public JSONObject getJsonObject() {
		JSONParser parser = new JSONParser();
		JSONObject js = new JSONObject();
		try {
			js = (JSONObject) parser.parse(new FileReader(file));
			return js;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves the value at the specified JSON path in the JSON file.
	 *
	 * @param jpath the JSON path
	 * @return the value at the specified JSON path
	 * @throws JsonPathException if an error occurs while parsing the JSON path
	 */
	public String valueAtJpath(String jpath) {
		try {
			return JsonPath.parse(this.getContent()).read(jpath).toString();
		} catch (JsonPathException e) {
			throw new JsonPathException();
		}
	}

	/**
	 * Retrieves the value at the specified JSON path from the given JSON string.
	 *
	 * @param jsonString the JSON string
	 * @param jpath      the JSON path
	 * @return the value at the specified JSON path
	 * @throws JsonPathException if an error occurs while parsing the JSON path
	 */
	public static String valueAtJpathFromJsonSting(String jsonString, String jpath) {
		try {
			return JsonPath.parse(jsonString).read(jpath).toString();
		} catch (JsonPathException e) {
			throw new JsonPathException();
		}
	}

	/**
	 * Updates the value at the specified JSON path in the given JSON string with the new value.
	 *
	 * @param jsonString the JSON string
	 * @param jpath      the JSON path
	 * @param newValue   the new value to be set
	 * @return the updated JSON string
	 */
	public static String updateValueAtJpath(String jsonString, String jpath, Object newValue) {
		try {
			if (jsonString != null) {
				return JsonPath.parse(jsonString).set(jpath, newValue, new Predicate[0]).jsonString();
			}
		} catch (InvalidJsonException e) {
			BaseClass.log().warn("Invalid Json :" + e.getMessage());
		}
		return jsonString;
	}

}
