package testBase;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static testBase.ThreadUtils.getITestContext;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.sikuli.script.Screen;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.xml.XmlTest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

import annotations.PopUp;
import dataUtils.RuntimeData;
import pageFactory.PageFactory;
import testBase.documetation.PDFReportObject;
import testBase.listners.API_Call;
import testBase.listners.EventHandler;
import testBase.listners.ExtentReportListener;
import testBase.listners.LogFormatter;
import testReportingAPI.TestResultsAPI;

/**
 * This class is the base class for all test cases.
 */
@Listeners(ExtentReportListener.class)
public class BaseClass {
	
	private static final String IMPLICIT_WAIT_TIME ="implicitlyWaitTime";   
	private static final String DOWNLOAD ="download";   
//	private static AmazonTextractClientBuilder clientBuilder = AmazonTextractClientBuilder.standard().withRegion(Regions.AP_SOUTH_1);
	/**
	 * This method is invoked before the test class is executed to launch the
	 * browser.
	 * 
	 * @param browserName The name of the browser to be launched (optional, default
	 *                    is 'chrome').
	 */
	@BeforeClass(alwaysRun = true)
	@Parameters({ "browserName" })
	public void launchBrowser(@Optional("chrome") String browserName) {
		try {
			String testCaseName = substringAfterLast(Reporter.getCurrentTestResult().getTestClass().getXmlClass().getName().trim(), ".");
			LogFormatter.createDownloadLogScreenshotDirectories(testCaseName);
			LogFormatter.initLogFormatter(testCaseName);

			ArtefactBuilder.initArtefactBuilder();
			Driver.initDriverForWeb(browserName);

			Page page = getDriver();
//			page.setViewportSize(1920, 1080);
			page.setDefaultTimeout(Integer.parseInt(getParameter(IMPLICIT_WAIT_TIME, "15")) * 1000);

			log().info(browserName + " browser is launched");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is invoked after the test class is executed to close the browser
	 * and generate artefacts.
	 * 
	 * @throws IOException If an I/O error occurs.
	 */
	@AfterClass(alwaysRun = true)
	public void endBrowser() throws IOException {
		
		String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"));
		String startTime = ThreadUtils.getPDFReportObj().getStartTime();
		
		ITestResult result = ThreadUtils.getPDFReportObj().getResult();
		String strResult = result.getStatus() == ITestResult.SUCCESS ? "Passed" : (result.getStatus() == ITestResult.FAILURE ? "Failed" : "Skipped");
		
		XmlTest xmlTest = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		String testCaseName = xmlTest.getName();

		String testClassName = "";
		String className = Reporter.getCurrentTestResult().getTestClass().getXmlClass().getName().trim();
		String lastClassName = xmlTest.getXmlClasses().get(xmlTest.getClasses().size() - 1).getName().trim();
		
		if (xmlTest.getClasses().size() == 1)
			testClassName = substringAfterLast(xmlTest.getClasses().get(0).getName().trim(), ".");
		else if (className.trim().equalsIgnoreCase(substringAfterLast(lastClassName, "."))) {
			testClassName = substringAfterLast(lastClassName, ".");
		} else {
			testClassName = substringAfterLast(className, ".");
		}
		
		try {
			Driver.quitDriver(testClassName);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		ArtefactBuilder.artefactDocxBuilder(testClassName, startTime, endTime, strResult);
		ArtefactBuilder.artefactPDFBuilder(testClassName, startTime, endTime, strResult);
		API_Call.publishToApi(result, testClassName, startTime, endTime);
		ATSSharepointActions.uploadFileToSharePoint(testClassName);
		
		new TestResultsAPI().processTestScript(result);
	}

	/**
	 * Returns the Page instance associated with the current thread.
	 * 
	 * @return The Page instance.
	 */
	public static Page getDriver() {
		PlaywrightDriver driver = ThreadUtils.getDriverRef();
		return driver != null ? driver.getPage() : null;
	}
	
	/**
	 * Returns the BrowserContext instance associated with the current thread.
	 * 
	 * @return The BrowserContext instance.
	 */
	public static BrowserContext getContext() {
		PlaywrightDriver driver = ThreadUtils.getDriverRef();
		return driver != null ? driver.getContext() : null;
	}
	/**
	 * Returns the Sikuli Screen instance associated with the current thread.
	 * @return The Sikuli Screen instance.
	 */
	public static Screen getScreen() {
		return ThreadUtils.getScreenRef();
	}

	/**
	 * Initializes the PageFactory for the specified component class using the Page
	 * instance.
	 * 
	 * @param coms The component class.
	 * @param <T>  The type of the component class.
	 * @return An instance of the component class.
	 */
	public static <T> T initElements(Class<T> coms) {
		return PageFactory.initElements(getDriver(), coms);
	}

	private static String getLocalClassParameter(String key) {
		String value = null;
		try {
			value = Reporter.getCurrentTestResult().getTestClass().getXmlClass().getLocalParameters().get(key);
		} catch (NullPointerException e) {
		}
		return value;
	}

	/**
	 * Retrieves the value of the specified parameter.
	 * 
	 * @param key The parameter key.
	 * @return The value of the parameter, or the default value if not found.
	 */
	public static String getParameter(String key) {
		try {
			String value = System.getProperty(key);
			String defValue = getITestContext().getCurrentXmlTest().getParameter(key);
			String defLocalValue = getLocalClassParameter(key);
			return isBlank(value) ? (isBlank(defLocalValue) ? defValue : defLocalValue) : value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the test description for the current thread's PDF report object. This
	 * will update the summary table in PDF document
	 * 
	 * @param testDescription
	 */
	public static void setTestDescription(String testDescription) {
		ThreadUtils.getPDFReportObj().setTestDescription(testDescription);
	}

	/**
	 * Sets the test details for the current thread's PDF report object. This will
	 * update the summary table in word document
	 * 
	 * @param reportObject The PDFReportObject containing the test details.
	 */
	public static void setTestDetails(PDFReportObject reportObject) {
		PDFReportObject p = ThreadUtils.getPDFReportObj();
		p.setDescription(reportObject.getDescription());
		p.setProcess(reportObject.getProcess());
		p.setUser(reportObject.getUser());
		p.setPrerequisites(reportObject.getPrerequisites());
		p.setNotes(reportObject.getNotes());
		p.setUsecaseId(reportObject.getUsecaseId());
	}

	/**
	 * Retrieves the value of the specified parameter with a default value if not
	 * found.
	 * 
	 * @param key          The parameter key.
	 * @param defaultValue The default value.
	 * @return The value of the parameter, or the default value if not found.
	 */
	public static String getParameter(String key, String defaultValue) {
		String value = getParameter(key);
		return (value != null) ? value : defaultValue;
	}

	/**
	 * Captures a screenshot with the given description.
	 * 
	 * @param description The description of the screenshot.
	 */
	public static void screenshot(String description) {
		byte[] data = null;
		if (!ThreadUtils.getIsScreen()) {
			try {
				Page page = getDriver();
				if (page != null) {
					data = page.screenshot(new Page.ScreenshotOptions().setTimeout(30000));
				}
			} catch (TimeoutError exp) {
				try {
					Robot robot = new Robot();
					Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
					BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(screenFullImage, "png", baos);
					baos.flush();
					data = baos.toByteArray();
				} catch (Exception e) {
					ThreadUtils.getLogger().error("[ERROR] Unable to capture the screenshot!", e);
				}
			}
		} else if (ThreadUtils.getIsScreen()) {
			try {
				Robot robot = new Robot();
				Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(screenFullImage, "png", baos);
				baos.flush();
				data = baos.toByteArray();
			} catch (Exception e) {
				log().error("Unable to capture the screenshot!", e);
			}
		}

		String fPath = ThreadUtils.getScreenshotDirectoryPath() + File.separator + RuntimeData.getRandomChars(9, 10)+ ".jpg";
		ThreadUtils.getSSObjRef().add(Arrays.asList(description, fPath));

		try {
			FileUtils.writeByteArrayToFile(new File(fPath), data);
			log().info("ScreenShot : " + description);
			writeDescriptionList(description);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeDescriptionList(String description) throws IOException {
		String filePath = ThreadUtils.getScreenshotDirectoryPath() + File.separator + "descriptionList.txt";

		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();

		FileUtils.writeStringToFile(file, description + "\n", true);
	}

	/**
	 * Pauses the execution for the specified duration.
	 * 
	 * @param timeout The pause duration in seconds.
	 */
	public static void pause(long timeout) {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs a click operation using Playwright.
	 * 
	 * @param locator The Locator to be clicked.
	 */
	public static void forceClick(Locator locator) {
		String stmt = "Click highlighted";
		String innerText = "";
		try {
			innerText = locator.innerText();
		} catch (Exception e) {
		}

		if (isNoneBlank(innerText))
			stmt = "Click '" + innerText + "'";

		ArtefactBuilder.takeArtefact(stmt, locator);
		locator.evaluate("element => element.click()");
	}

	/**
	 * Performs a type operation using Playwright to enter text into a locator.
	 * 
	 * @param locator The Locator to enter text into.
	 * @param text    The text to be entered.
	 */
	public static void forceType(Locator locator, String text) {
		locator.evaluate("element => element.value = '" + text + "'");
		ArtefactBuilder.takeArtefact("Enter '" + text + "' in highlighted field", locator);
	}

	/**
	 * Waits for jQuery and JavaScript to finish loading.
	 * 
	 * @param timeInSeconds The maximum time to wait in seconds.
	 */
	public static void waitForJQueryAndJSToLoad(long timeInSeconds) {
		Page page = getDriver();
		if (page != null) {
			page.waitForFunction("() => document.readyState == 'complete' && (!window.jQuery || jQuery.active == 0)",
					new Page.WaitForFunctionOptions().setTimeout(timeInSeconds * 1000));
		}
	}

	/**
	 * Gets the list of downloaded files in the temporary directory.
	 * 
	 * @return An array of File objects representing the downloaded files.
	 */
	public static File[] getDownloadedFileslist() {
		File[] files = null;
		try {
			files = new File(ThreadUtils.getTempDirectoryPath() + DOWNLOAD).listFiles();
		} catch (SecurityException e) {
			log().warn("Verify the read permissions " + e.toString());
		} catch (NullPointerException e) {
			log().warn("Verify the path exists or not: " + e.toString());
		}
		return files;
	}

	/**
	 * Gets the downloaded file with the specified filename.
	 * 
	 * @param filename The name of the downloaded file.
	 * @return The File object representing the downloaded file, or null if it
	 *         doesn't exist.
	 */
	public static File getDownloadedFile(String filename) {
		File file = null;
		try {
			file = new File(ThreadUtils.getTempDirectoryPath() + DOWNLOAD + File.separator + filename);
			if (file.exists()) {
				return file;
			} else {
				log().warn(filename + " is not available in " + ThreadUtils.getTempDirectoryPath() + DOWNLOAD);
			}
		} catch (Exception e) {
			log().warn("Verify the read path/permissions " + e.toString());
		}
		return null;
	}

	/**
	 * Gets the latest file from the specified directory.
	 * 
	 * @param dirPath The path to the directory.
	 * @return The File object representing the latest file, or null if the
	 *         directory is empty.
	 */
	public static File getLatestFilefromDir(String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}

		File lastModifiedFile = files[0];
		for (int i = 0; i < files.length; i++) {
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

	/**
	 * Returns the logger instance.
	 * 
	 * @return The logger instance.
	 */
	public static Logger log() {
		return ThreadUtils.getLogger();
	}

	/**
	 * Returns the title of the current page.
	 * 
	 * @return The title of the current page.
	 */
	public static String title() {
		Page page = getDriver();
		return page != null ? page.title() : "";
	}

	/**
	 * Switches to a window with the specified page title.
	 * 
	 * @param pageTitle The title of the page to switch to.
	 */
	public static Page switchToWindow(String pageTitle) {
		for (Page page : getContext().pages()) {
			if (page.title().equals(pageTitle)) {
				page.bringToFront();
				page.waitForLoadState();
				PlaywrightDriver driver = new PlaywrightDriver(ThreadUtils.getDriverRef().getPlaywright(),
						ThreadUtils.getDriverRef().getBrowser(), getContext(), page);
				ThreadUtils.setDriverRef(driver);
				ArtefactBuilder.takeArtefact("Switch to window '" + pageTitle + "'", null);
				break;
			}
		}
		return getDriver();
	}
	
	/**
	 * Switches to a window with the specified index.
	 * 
	 * @param pageTitle The title of the page to switch to.
	 * @throws Exception
	 */
	public static Page switchToWindow(int index) {
		getDriver().waitForCondition(() -> getContext().pages().size() > 1);
		// Get all pages (tabs/windows) in the context
		java.util.List<Page> allPages = ThreadUtils.getDriverRef().getContext().pages();
		// Switch to the page by index (e.g., index 1 for the second page)
		Page pageByIndex = allPages.get(index);
		pageByIndex.bringToFront();
		String salutation = "th";
		switch (index % 10) {
		case 1:
			salutation = "st";
			break;
		case 2:
			salutation = "nd";
			break;
		case 3:
			salutation = "rd";
			break;
		default:
			salutation = "th";
		}
		PlaywrightDriver driver = new PlaywrightDriver(ThreadUtils.getDriverRef().getPlaywright(),
				ThreadUtils.getDriverRef().getBrowser(), getContext(), pageByIndex);
		ThreadUtils.setDriverRef(driver);
		ArtefactBuilder.takeArtefact("Switch to '" + String.valueOf(index) + salutation + "' window ", null);
		return getDriver();
	}

	/**
	 * Sets a value in the cache.
	 * 
	 * @param object The key for the cache entry.
	 * @param value  The value to be stored in the cache.
	 */
	public static void setCache(String object, String value) {
		ThreadUtils.getITestContext().getSuite().setAttribute(object, value);
	}

	/**
	 * Retrieves a value from the cache.
	 * 
	 * @param object The key for the cache entry.
	 * @return The value from the cache, or null if it doesn't exist.
	 */
	public static Object getCache(String object) {
		return ThreadUtils.getITestContext().getSuite().getAttribute(object);
	}

	/**
	 * Clicks on the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to click on.
	 */
	public static void click(String stepDescription, Locator locator) {
	    if (isNotBlank(stepDescription)) {
	        ArtefactBuilder.setCustAct(false);
	        ArtefactBuilder.artefactSS(stepDescription, locator);
	        locator.click();
	        ArtefactBuilder.setCustAct(true);
	    } else {
	        locator.click();
	    }
	}


	/**
	 * Clicks on the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to click on.
	 */
	public static void click(String locator) {
		Locator lc = getDriver().locator(locator);
		new EventHandler().beforeClick(lc);
		lc.click();
	}

	/**
	 * Clicks on the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to click on.
	 */
	public static void click(FrameLocator frame, String locator) {
		Locator lc = frame.locator(locator);
		new EventHandler().beforeClick(lc);
		lc.click();
	}

	/**
	 * Fills text into the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to fill text into.
	 * @param text            The text to fill.
	 */
	public static void fill(String stepDescription, Locator locator, String text) {
		if (isNotBlank(stepDescription)) {
			ArtefactBuilder.setCustAct(false);
			locator.fill(text);
			ArtefactBuilder.artefactSS(stepDescription, locator);
			ArtefactBuilder.setCustAct(true);
		} else {
			locator.fill(text);
		}
	}

	/**
	 * Fills text into the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to fill text into.
	 * @param text            The text to fill.
	 */
	public static void fill(Locator locator, String text) {
		locator.fill(text);
		new EventHandler().afterFill(locator, text);
	}

	/**
	 * Fills text into the specified locator with an optional step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to fill text into.
	 * @param text            The text to fill.
	 */
	public static void fill(FrameLocator frame, String locator, String text) {
		Locator lc = frame.locator(locator);
		lc.fill(text);
		new EventHandler().afterFill(lc, text);
	}

	/**
	 * Performs a forceful click on the specified locator with an optional step
	 * description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to perform a forceful click on.
	 */
	public static void forceClick(String stepDescription, Locator locator) {
		if (isNotBlank(stepDescription)) {
			ArtefactBuilder.takeArtefact(stepDescription, locator);
			locator.click(new Locator.ClickOptions().setForce(true).setTimeout(30000));
		} else {
			forceClick(locator);
		}
	}

	/**
	 * Performs a forceful type action on the specified locator with an optional
	 * step description.
	 * 
	 * @param stepDescription The description of the step.
	 * @param locator         The Locator to perform a forceful type action on.
	 * @param text            The text to type.
	 */
	public static void forceType(String stepDescription, Locator locator, String text) {
		if (isNotBlank(stepDescription)) {
			locator.fill(text);
			ArtefactBuilder.takeArtefact(stepDescription, locator);
		} else {
			forceType(locator, text);
		}
	}

	public static void type(Locator locator, String value) {
        if (locator != null) {
        	locator.waitFor();
        	locator.fill(value);
            new EventHandler().afterFill(locator, value);
            locator.press("Tab");
        } else {
            log().error("Locator is null or empty for type action");
        }
    }
	
	private static Locator getElement(String locator) {
        if (locator.contains("iframe:")) {
            String[] parts = locator.split("iframe:", 2);
            String iframeSelector = parts[0];
            String elementSelector = parts[1];
            return getDriver().frameLocator(iframeSelector).locator(elementSelector);
        }
        return getDriver().locator(locator);
    }
	
	public static String getXpathString(Locator e) {
		Map<String, String> locator = getLocatorSelector(e);
		return locator.get("selector").equalsIgnoreCase("xpath") ? locator.get("value") : null;
	}
	
	private static Map<String, String> getLocatorSelector(Locator e) {
		Map<String, String> locator = new HashMap<>();
		String[] pathVariables = null;
		String selector = "unknown";
		String value = "";

		try {
			String str = e.toString();

			if (str.contains("Locator@")) {
				String xPathString = str.replace("Locator@", "");
				pathVariables = xPathString.split(">>");
				selector = "xpath";
				value = pathVariables[pathVariables.length - 1].trim();
			} else {
				if (str.startsWith("//")) {
					selector = "xpath";
					value = str;
				} else if (str.matches("^(xpath)=.*")) {
					selector = "xpath";
					value = str.replace("xpath=", "");
				}
			}
			locator.put("selector", selector);
			locator.put("value", value);
			locator.put("string", str);

		} catch (Exception ex) {
			System.out.println("Error parsing locator: " + ex.getMessage());
		}

		return locator;
	}

	/**
	 * step/method which are implemented using this methods will not be record in Artefact Document.
	 * @param popUp
	 */
	public static void noRecordInDocument(PopUp popUp) {
		String flag = getParameter("generateDocument", "false");
		try {
			Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().setParameters(Maps.newHashMap(ImmutableMap.of("generateDocument", "false")));
			popUp.handle();
		} finally {
			Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().setParameters(Maps.newHashMap(ImmutableMap.of("generateDocument", flag)));
		}
	}

	/**
	 * Extract text from screenshot
	 * @param imagePath complete path of the screenshot.
	 * @param regexCode text to be extracted from specified pattern
	 */
	/*
	 * public static String extractImageText(String regexCode) throws IOException {
	 * clientBuilder.setCredentials(new AWSStaticCredentialsProvider( new
	 * BasicAWSCredentials("<AWS_ACCESS_KEY_ID>",
	 * "<AWS_SECRET_ACCESS_KEY>"))); ByteBuffer imageBytes; try
	 * (InputStream inputStream = new FileInputStream(new
	 * File(ThreadUtils.getSSObjRef().get(ThreadUtils.getSSObjRef().size()-1).get(1)
	 * ))) { imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream)); }
	 * 
	 * AmazonTextract client = clientBuilder.build(); DetectDocumentTextRequest
	 * request = new DetectDocumentTextRequest().withDocument( new
	 * Document().withBytes(imageBytes));
	 * 
	 * DetectDocumentTextResult result = client.detectDocumentText(request);
	 * 
	 * // Extract text based on specified regex String extractedText =
	 * extractText(result.toString(), regexCode);
	 * log().info("INFO : ========Order number: "+extractedText+"========="); return
	 * extractedText; }
	 */

	/**
	 * Extract required text from JSON response
	 */
   public static String extractText(String response, String regex) {
       Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
       Matcher matcher = pattern.matcher(response);
       if (matcher.find()) {
           return matcher.group(1);
       }
       return null;
   }
   
   /**
	 * Handles a pop-up window.
	 * @param popUp The PopUp object representing the pop-up window.
	 */
//	public static void handlePopUp(PopUp popUp) {
//
//		String parentWindow = opaqueWindow();
//		String parentTitle = title();
//		String subWindowHandler = null;
//		Set<String> handles = getDriver().getWindowHandles();
//		log().warn(String.format("Parent Window=%s", parentTitle));
//		Iterator<String> iterator = handles.iterator();
//
//		while (iterator.hasNext()) {
//			subWindowHandler = iterator.next();
//			if (!StringUtils.equals(parentWindow, subWindowHandler)) {
//				break;
//			}
//		}
//
//		getDriver().switchTo().window(subWindowHandler);
//		log().warn(String.format("switched to %s Window", title()));
//		popUp.handle();
//		getDriver().switchTo().window(parentWindow);
//		log().warn(String.format("switched to Parent- %s Window ", parentTitle));
//	}
}