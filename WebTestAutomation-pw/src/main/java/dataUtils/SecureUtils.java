package dataUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SecureUtils {
	
	private SecureUtils() {}
	
	private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String NUMBER = "0123456789";

	/**
	 * Returns a SecureRandom instance using the "SHA1PRNG" algorithm.
	 *
	 * @return a SecureRandom instance
	 */
	public static SecureRandom random() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException var1) {
			System.out.println("Exception occrred while generating a secure random!" + var1);
			return null;
		}
	}

	/**
	 * Returns a SecureRandom instance initialized with random bytes generated from the current timestamp.
	 *
	 * @return a SecureRandom instance with current timestamp as seed
	 */
	public static SecureRandom randomByCurrentTimeStamp() {
		try {
			SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
			byte[] currentTimeStamp = Long.toHexString(System.currentTimeMillis()).getBytes();
			secRandom.nextBytes(currentTimeStamp);
			return secRandom;
		} catch (NoSuchAlgorithmException var2) {
			System.out.println("Exception occrred while generating a secure random!" + var2);
			return null;
		}
	}

	/**
	 * Generates a random string of the specified length.
	 *
	 * @param length   the length of the random string
	 * @param letters  flag indicating whether letters should be included in the string
	 * @param numbers  flag indicating whether numbers should be included in the string
	 * @return a randomly generated string
	 */
	public static String randomString(int length, boolean letters, boolean numbers) {
		try {
			SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
			StringBuilder stringBuilder = new StringBuilder();
			if (length < 0) {
				throw new IllegalArgumentException("Requested random string length is less than 0.");
			} else if (length == 0) {
				return "";
			} else {
				String characters = null;
				if (letters && numbers) {
					characters = CHAR_LOWER + CHAR_UPPER + NUMBER;
				} else if (!letters && numbers) {
					characters = "0123456789";
				} else {
					if (!letters || numbers) {
						return "";
					}

					characters = "abcdefghijklmnopqrstuvwxyz" + CHAR_UPPER;
				}

				for (int i = 0; i < length; ++i) {
					int randomIndex = secRandom.nextInt(characters.length());
					stringBuilder.append(characters.charAt(randomIndex));
				}

				return stringBuilder.toString();
			}
		} catch (NoSuchAlgorithmException var8) {
			System.out.println("Exception occrred while generating a secure random!" + var8);
			return null;
		}
	}
}