package scripts;

import org.testng.annotations.Test;

import functions.testFucntion;
import pages.TestPage;
import testBase.ArtefactBuilder;
import testBase.ArtefactBuilder;
import testBase.BaseClass;

public class SampleTest extends BaseClass {

	

	@Test
	public void SampleTest() throws Exception {
		
		getDriver().navigate("https://www.google.com");
//		pause(10);
		TestPage page = initElements(TestPage.class);
		
		page.googleInputDynamic.getLocator("q").first().fill("testUser");
		ArtefactBuilder.artefactSS("SampleTest", page.googleInputDynamic.getLocator("q"));

	}

	
}
