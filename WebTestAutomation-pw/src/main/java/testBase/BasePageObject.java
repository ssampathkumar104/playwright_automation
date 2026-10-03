package testBase;

import com.microsoft.playwright.Page;

/**
* The BasePageObject class is a generic class that serves as the base class for page objects in the test framework.
* It provides a Page instance and getter method to access the page.
* The class is parameterized with the type B, which represents the specific subclass of BasePageObject.
*/
public class BasePageObject<B extends BasePageObject<B>>{
	private Page page;

	/**
	 * Returns the Page instance associated with the page object.
	 *
	 * @return the Page instance
	 */
	public Page getPage() {
		return this.page;
	}

	/**
	 * Constructs a new instance of BasePageObject and initializes the page with the page reference from the thread.
	 */
	public BasePageObject() {
		PlaywrightDriver driver = ThreadUtils.getDriverRef();
		this.page = driver != null ? driver.getPage() : null;
	}
}