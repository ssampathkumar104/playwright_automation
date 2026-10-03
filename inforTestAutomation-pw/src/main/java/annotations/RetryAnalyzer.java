package annotations;


import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer is a class that implements the IRetryAnalyzer interface.
 * It is responsible for determining how many times a test should be retried.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

	int counter = 0;
	int currentTry =0;
	int maxTry=2;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.testng.IRetryAnalyzer#retry(org.testng.ITestResult)
	 * 
	 * This method decides how many times a test needs to be rerun. TestNg will
	 * call this method every time a test fails. So we can put some code in here
	 * to decide when to rerun the test.
	 * 
	 * Note: This method will return true if a tests needs to be retried and
	 * false it not.
	 *
	 */

	/**
	 * This method is called by TestNG when a test fails to determine if it should be retried.
	 * It returns true if the test should be retried, and false otherwise.
	 *
	 * @param result the test result
	 * @return true if the test should be retried, false otherwise
	 */
	@Override
	public boolean retry(ITestResult result) {

		if (currentTry<maxTry) {
			System.out.println("Retrying");
			currentTry++;
			return true;
		}else {
			return false;
		}
	}
}