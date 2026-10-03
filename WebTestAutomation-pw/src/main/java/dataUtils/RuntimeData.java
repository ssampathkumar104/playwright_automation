package dataUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.fluttercode.datafactory.AddressDataValues;
import org.fluttercode.datafactory.ContentDataValues;
import org.fluttercode.datafactory.NameDataValues;
import org.fluttercode.datafactory.impl.DefaultAddressDataValues;
import org.fluttercode.datafactory.impl.DefaultContentDataValues;
import org.fluttercode.datafactory.impl.DefaultNameDataValues;

/**
*
* Provides runtime data generation methods for various purposes.
*/
public class RuntimeData {
	
	private static SecureRandom random = SecureUtils.random();
	private static NameDataValues nameDataValues = new DefaultNameDataValues();
	private static AddressDataValues addressDataValues = new DefaultAddressDataValues();
	private static ContentDataValues contentDataValues = new DefaultContentDataValues();

	/**
	 * Returns the current date/time formatted according to the specified date format.
	 *
	 * @param dateFormat the format string for the date/time
	 * @return the formatted date/time string
	 */
	public static String getDateParam(String dateFormat) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		return dateFormatter.format(new Date());
	}

	/**
	 * Generates a random string of the specified length, containing letters and/or numbers.
	 *
	 * @param length  the length of the random string
	 * @param letters whether to include letters in the random string
	 * @param numbers whether to include numbers in the random string
	 * @return the generated random string
	 */
	public static String getRandomString(int length, boolean letters, boolean numbers) {
		String randomString = SecureUtils.randomString(length, letters, numbers);
		return randomString;
	}

	/**
	 * Generates a random integer.
	 *
	 * @return the generated random integer
	 */
	public static int getNumber() {
		return getNumberBetween(1, Integer.MAX_VALUE);
	}

	/**
	 * Generates a random integer between the specified minimum and maximum values (inclusive).
	 *
	 * @param minValue the minimum value
	 * @param maxValue the maximum value
	 * @return the generated random integer
	 * @throws IllegalArgumentException if the minimum value is greater than the maximum value
	 */
	public static int getNumberBetween(int minValue, int maxValue) {
		if (maxValue < minValue) {
			throw new IllegalArgumentException(
					String.format("Minimum value must be less than maxValue (min=%d, max=%d)", minValue, maxValue));
		} else {
			return (int) (SecureUtils.random().nextDouble() * (maxValue - minValue)) + minValue;
		}
	}

	/**
	 * Generates a random integer up to the specified maximum value (inclusive).
	 *
	 * @param max the maximum value
	 * @return the generated random integer
	 * @throws IllegalArgumentException if the maximum value is less than zero
	 */
	public static int getNumberUpTo(int max) {
		if (0 > max) {
			throw new IllegalArgumentException(String.format("max value must be positive (max=%d)", max));
		} else {
			return getNumberBetween(0, max);
		}
	}

	/**
	 * Returns a random first name.
	 *
	 * @return the random first name
	 */
	public static String getFirstName() {
		return getRandomItem(nameDataValues.getFirstNames());
	}

	/**
	 * Returns a random full name consisting of a first name and a last name.
	 *
	 * @return the random full name
	 */
	public static String getName() {
		return getFirstName() + " " + getFirstName();
	}

	/**
	 * Returns a random last name.
	 *
	 * @return the random last name
	 */
	public static String getLastName() {
		return getRandomItem(nameDataValues.getLastNames());
	}

	/**
	 * Returns a random street name.
	 *
	 * @return the random street name
	 */
	public static String getStreetName() {
		return getRandomItem(addressDataValues.getStreetNames());
	}

	/**
	 * Returns a random street suffix.
	 *
	 * @return the random street suffix
	 */
	public static String getStreetSuffix() {
		return getRandomItem(addressDataValues.getAddressSuffixes());
	}

	/**
	 * Returns a random city name.
	 *
	 * @return the random city name
	 */
	public static String getCity() {
		return getRandomItem(addressDataValues.getCities());
	}

	/**
	 * Returns a random address in the format "number street, city".
	 *
	 * @return the random address
	 */
	public static String getAddress() {
		int num = 1 + random.nextInt(800);
		return num + " " + getStreetName() + "," + getCity();
	}

	/**
	 * Returns a Date object representing the specified year, month, and date.
	 *
	 * @param year  the year
	 * @param month the month (0-based index)
	 * @param date  the day of the month
	 * @return the generated Date object
	 */
	public static Date getDate(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month, date);
		return calendar.getTime();
	}

	/**
	 * Returns a Date object that is a random number of days away from the specified base date.
	 *
	 * @param baseDate         the base date
	 * @param minDaysFromDate  the minimum number of days from the base date
	 * @param maxDaysFromDate  the maximum number of days from the base date
	 * @return the generated Date object
	 */
	public static Date getDate(Date baseDate, int minDaysFromDate, int maxDaysFromDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(baseDate);
		int diff = minDaysFromDate + random.nextInt(maxDaysFromDate - minDaysFromDate);
		calendar.add(5, diff);
		return calendar.getTime();
	}

	/**
	 * Returns a Date object that is a random date between the specified minimum and maximum dates.
	 *
	 * @param minDate the minimum date
	 * @param maxDate the maximum date
	 * @return the generated Date object
	 */
	public static Date getDateBetween(Date minDate, Date maxDate) {
		long seconds = (maxDate.getTime() - minDate.getTime()) / 1000L;
		seconds = (long) (random.nextDouble() * seconds);
		Date result = new Date();
		result.setTime(minDate.getTime() + seconds * 1000L);
		return result;
	}

	/**
	 * Generates a random text string of the specified length.
	 *
	 * @param length the length of the random text
	 * @return the generated random text string
	 */
	public static String getRandomText(int length) {
		return getRandomText(length, length);
	}

	/**
	 * Generates a random text string within the specified minimum and maximum lengths.
	 *
	 * @param minLength the minimum length of the random text
	 * @param maxLength the maximum length of the random text
	 * @return the generated random text string
	 * @throws IllegalArgumentException if the minimum length or maximum length is negative, or if the minimum length is greater than the maximum length
	 */
	public static String getRandomText(int minLength, int maxLength) {
		validateMinMaxParams(minLength, maxLength);
		StringBuilder sb = new StringBuilder(maxLength);
		int length = minLength;
		if (maxLength != minLength) {
			length = minLength + random.nextInt(maxLength - minLength);
		}

		while (length > 0) {
			if (sb.length() != 0) {
				sb.append(" ");
				--length;
			}

			String word = getRandomWord(length);
			sb.append(word);
			length -= word.length();
		}

		return sb.toString();
	}

	/**
	 * Validates the minimum and maximum length parameters for random text generation.
	 *
	 * @param minLength the minimum length
	 * @param maxLength the maximum length
	 * @throws IllegalArgumentException if the minimum length or maximum length is negative, or if the minimum length is greater than the maximum length
	 */
	private static void validateMinMaxParams(int minLength, int maxLength) {
		if (minLength < 0) {
			throw new IllegalArgumentException("Minimum length must be a non-negative number");
		} else if (maxLength < 0) {
			throw new IllegalArgumentException("Maximum length must be a non-negative number");
		} else if (maxLength < minLength) {
			throw new IllegalArgumentException(String
					.format("Minimum length must be less than maximum length (min=%d, max=%d)", minLength, maxLength));
		}
	}

	/**
	 * Returns a random word.
	 *
	 * @return the random word
	 */
	public static String getRandomWord() {
		return getRandomItem(contentDataValues.getWords());
	}

	/**
	 * Returns a random word of the specified length.
	 *
	 * @param length the length of the random word
	 * @return the random word
	 */
	public static String getRandomWord(int length) {
		return getRandomWord(length, length);
	}

	/**
	 * Returns a random word of the specified length.
	 *
	 * @param length      the length of the random word
	 * @param exactLength whether the length of the word should be exactly the specified length
	 * @return the random word
	 */
	public static String getRandomWord(int length, boolean exactLength) {
		return exactLength ? getRandomWord(length, length) : getRandomWord(0, length);
	}

	/**
	 * Returns a random word within the specified minimum and maximum lengths.
	 *
	 * @param minLength the minimum length of the random word
	 * @param maxLength the maximum length of the random word
	 * @return the random word
	 * @throws IllegalArgumentException if the minimum length or maximum length is negative, or if the minimum length is greater than the maximum length
	 */
	public static String getRandomWord(int minLength, int maxLength) {
		validateMinMaxParams(minLength, maxLength);
		if (maxLength == 1) {
			return chance(50) ? "a" : "I";
		} else {
			String[] words = contentDataValues.getWords();
			int pos = random.nextInt(words.length);

			for (int i = 0; i < words.length; ++i) {
				int idx = (i + pos) % words.length;
				String test = words[idx];
				if (test.length() >= minLength && test.length() <= maxLength) {
					return test;
				}
			}

			return getRandomChars(minLength, maxLength);
		}
	}

	/**
	 * Returns a random character.
	 *
	 * @return the random character
	 */
	public static char getRandomChar() {
		return (char) (random.nextInt(26) + 97);
	}

	public static String getRandomChars(int length) {
		return getRandomChars(length, length);
	}

	/**
	 * Returns a random string of characters within the specified minimum and maximum lengths.
	 *
	 * @param minLength the minimum length of the random string
	 * @param maxLength the maximum length of the random string
	 * @return the random string
	 * @throws IllegalArgumentException if the minimum length or maximum length is negative, or if the minimum length is greater than the maximum length
	 */
	public static String getRandomChars(int minLength, int maxLength) {
		validateMinMaxParams(minLength, maxLength);
		StringBuilder sb = new StringBuilder(maxLength);
		int length = minLength;
		if (maxLength != minLength) {
			length = minLength + random.nextInt(maxLength - minLength);
		}

		while (length > 0) {
			sb.append(getRandomChar());
			--length;
		}

		return sb.toString();
	}

	/**
	*
	* Generates a string of random numbers with the specified number of digits.
	*
	* @param digits the number of digits in the generated number
	*
	* @return the generated number as a string
	*/
	public static String getNumberText(int digits) {
		String result = "";

		for (int i = 0; i < digits; ++i) {
			result = result + random.nextInt(10);
		}

		return result;
	}

	/**
	* 
	* Generates a random business name by combining a city name and a random business type.
	* 
	* @return the generated business name
	*/
	public static String getBusinessName() {
		return getCity() + " " + getRandomItem(contentDataValues.getBusinessTypes());
	}

	/**
	*
	* Generates a random email address.
	*
	* @return the generated email address
	*/
	public static String getEmailAddress() {
		int test = random.nextInt(100);
		String email = "";
		if (test < 50) {
			email = getFirstName().charAt(0) + getLastName();
		} else {
			email = getRandomWord() + getRandomWord();
		}

		if (random.nextInt(100) > 80) {
			email = email + random.nextInt(100);
		}

		email = email + "@" + getRandomItem(contentDataValues.getEmailHosts()) + "."
				+ getRandomItem(contentDataValues.getTlds());
		return email.toLowerCase();
	}

	/**
	*
	* Returns a random item from a list with a specified probability, or a default item if the probability is not met.
	* @param <T> the type of the items in the list
	* @param items the list of items
	* @param probability the probability of selecting an item from the list (between 0 and 100)
	* @param defaultItem the default item to return if the probability is not met
	* @return the randomly selected item from the list or the default item
	* @throws IllegalArgumentException if the list is null or empty
	*/
	public static <T> T getItem(List<T> items, int probability, T defaultItem) {
		if (items == null) {
			throw new IllegalArgumentException("Item list cannot be null");
		} else if (items.isEmpty()) {
			throw new IllegalArgumentException("Item list cannot be empty");
		} else {
			return chance(probability) ? items.get(random.nextInt(items.size())) : defaultItem;
		}
	}

	/**
	*
	* Returns a random item from an array with a specified probability, or a default item if the probability is not met.
	* @param <T> the type of the items in the array
	* @param items the array of items
	* @param probability the probability of selecting an item from the array (between 0 and 100)
	* @param defaultItem the default item to return if the probability is not met
	* @return the randomly selected item from the array or the default item
	* @throws IllegalArgumentException if the array is null or empty
	*/
	public static <T> T getItem(T[] items, int probability, T defaultItem) {
		if (items == null) {
			throw new IllegalArgumentException("Item array cannot be null");
		} else if (items.length == 0) {
			throw new IllegalArgumentException("Item array cannot be empty");
		} else {
			return chance(probability) ? items[random.nextInt(items.length)] : defaultItem;
		}
	}

	/**
	*
	* Returns a boolean value with a specified chance of being true.
	* @param chance the chance of the boolean value being true (between 0 and 100)
	* @return true if the random value is less than the chance, false otherwise
	*/
	public static boolean chance(int chance) {
		return random.nextInt(100) < chance;
	}

	/**
	*
	* Generates a username by appending a string and a random character sequence of specified length.
	*
	* If the append string is not provided, a random character sequence of the specified length is generated.
	*
	* If the length is 0, a default length of 8 is used.
	*
	* @param appendString the string to append to the generated username (can be null or empty)
	*
	* @param length the desired length of the generated username
	*
	* @return the generated username
	*/
	public static String usernameHelper(String appendString, int length) {
		String username = "";
		if (appendString != null && !"".equals(appendString)) {
			int appendStringlength = appendString.length();
			if (length > 0) {
				username = getRandomChars(length - appendStringlength);
				username = appendString.concat(username);
			} else {
				username = appendString.concat(getRandomChars(8));
			}
		} else if (length > 0) {
			username = getRandomChars(length);
		} else {
			username = getRandomChars(8);
		}

		return username;
	}

	/**
	 * Returns the current IP address.
	 *
	 * @return the IP address
	 */
	public static String getIpAddress() {
		String ipAddress = "";

		try {
			InetAddress ip = InetAddress.getLocalHost();
			ipAddress = ip.getHostAddress();
		} catch (UnknownHostException var3) {
			var3.printStackTrace();
		}

		return ipAddress;
	}

	/**
	*
	* Retrieves the JBoss node name from the system properties.
	*
	* @return the JBoss node name, or an empty string if it is not available
	*/
	public static String getNodeName() {
		String jbossNodeName = "";

		try {
			jbossNodeName = System.getProperty("jboss.node.name");
		} catch (Exception var2) {
			var2.printStackTrace();
		}

		return jbossNodeName;
	}

	/**
	*
	* Retrieves the host name of the current machine.
	*
	* @return the host name, or an empty string if it is not available
	*/
	public static String getHostName() {
		String hostName = "";

		try {
			InetAddress ip = InetAddress.getLocalHost();
			hostName = ip.getHostName();
		} catch (Exception var3) {
			System.err.println(var3.getMessage());
		}

		return hostName;
	}

	/**
	*
	* Returns a random item from an array.
	* @param <T> the type of the items in the array
	* @param items the array of items
	* @return a randomly selected item from the array
	*/
	private static <T> T getRandomItem(T[] items) {
		SecureRandom random = SecureUtils.random();
		int randomInt = random.nextInt(items.length);
		return items[randomInt];
	}
}