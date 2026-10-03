package testBase.listners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
*
* Provides runtime data generation methods for various purposes.
*/
public class RetryAnalyzer implements IRetryAnalyzer {

	private static final int retryLimit = 3;
	private int counter = 0;

	@Override
	public boolean retry(ITestResult result) {
		System.out.println("Into the retry analyser " + counter);
		if (counter < retryLimit && result.getStatus() != ITestResult.SUCCESS) {
			this.counter++;
			System.err.println("counter : " + counter);
			return true;
		}
		return false;
	}

}
