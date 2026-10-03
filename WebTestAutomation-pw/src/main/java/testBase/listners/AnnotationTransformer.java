package testBase.listners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
*
* Implementation of {@link IAnnotationTransformer} interface that allows modifying TestNG test method annotations.
*/
public class AnnotationTransformer implements IAnnotationTransformer {

	/**
	 * Transforms the TestNG test method annotation by setting the retry analyzer if not already defined.
	 *
	 * @param annotation       the TestNG test annotation being transformed
	 * @param testClass        the test class containing the test method
	 * @param testConstructor  the test constructor
	 * @param testMethod       the test method being transformed
	 */
	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

//		IRetryAnalyzer retry = annotation.getRetryAnalyzer();
//		if (retry == null) {
//			annotation.setRetryAnalyzer(testBase.listners.RetryAnalyzer.class);
//		}
	}

}
