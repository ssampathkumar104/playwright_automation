package testBase;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.sikuli.script.Screen;
import org.testng.ITestContext;
import org.testng.Reporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import testBase.documetation.ArtefactObject;
import testBase.documetation.PDFReportObject;


/**
* The ThreadUtils class provides utility methods and thread-local variables for multi-threaded execution.
* It includes methods for managing screen capture, WebDriver instance, logger, data folder path,
* temporary directory path, test context, PDF report object, custom flags, artifact objects,
* ExtentTest instance, and various directory paths.
*/
public class ThreadUtils {
	private ThreadUtils() {
	}

	// Thread-local variables for managing screen capture, WebDriver instance, logger, data folder path,
	// temporary directory path, test context, PDF report object, custom flags, artifact objects,
	// ExtentTest instance, and various directory paths
	private static ThreadLocal<Boolean> isScreen = new ThreadLocal<>();
	private static ThreadLocal<Screen> sikuli = new ThreadLocal<>();
	private static final ThreadLocal<PlaywrightDriver> driverRef = new ThreadLocal<>();
	
	private static ThreadLocal<Logger> logger = new ThreadLocal<>();
	
	private static ThreadLocal<String> dataFolder = new ThreadLocal<>();
	private static ThreadLocal<String> tempDirectoryPath = new ThreadLocal<>();
	private static ThreadLocal<String> screenshotDirectoryPath = new ThreadLocal<>();
	
	private static ThreadLocal<ITestContext> testContext = new ThreadLocal<>();
	
	private static ThreadLocal<Boolean> custFlagForPDF = new ThreadLocal<>();
	private static ThreadLocal<List<List<String>>> ssObj = new ThreadLocal<>();
	private static ThreadLocal<List<ArtefactObject>> artefactObj = new ThreadLocal<>();

	private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
	private static ThreadLocal<String> extentReportPath = new ThreadLocal<>();
	private static ThreadLocal<String> pdfReportPath = new ThreadLocal<>();
	
	private static ThreadLocal<PDFReportObject> pdfReportObj = new ThreadLocal<>();
	private static ThreadLocal<ExtentReports> executionReport = new ThreadLocal<>();
	private static ThreadLocal<String> failureSSBase64 = new ThreadLocal<>();
	
	// read-local variables for managing suiteID, caseID and Script ID's created in DB
	private static final Map<Long, Long> caseIdMap = new ConcurrentHashMap<>();
	private static final Map<Long, Long> scriptIdMap = new ConcurrentHashMap<>(); 
	private static final Map<Long, Long> suiteIdMap = new ConcurrentHashMap<>(); 
	
	
	public static synchronized Long  getSuiteID() {
		return suiteIdMap.get(Thread.currentThread().getId());
	}

	public static synchronized void setSuiteID(Long obj) {
		suiteIdMap.put(Thread.currentThread().getId(), obj);
	}
	
	public static void removeSuiteID() {
		suiteIdMap.remove(Thread.currentThread().getId());
	}
	
	public static synchronized Long  getScriptID() {
		return scriptIdMap.get(Thread.currentThread().getId());
	}

	public static synchronized void setScriptID(Long obj) {
		scriptIdMap.put(Thread.currentThread().getId(), obj);
	}
	
	public static void removeScriptID() {
		scriptIdMap.remove(Thread.currentThread().getId());
	}
	
	/*============================================================================================*/	

	public static synchronized void setCaseID(Long obj) {
//		System.err.println("setCaseID " + Thread.currentThread().getId());
	    caseIdMap.put(Thread.currentThread().getId(), obj);
	}

	public static synchronized Long getCaseID() {
//		System.err.println("getCaseID " + Thread.currentThread().getId());
	    return caseIdMap.get(Thread.currentThread().getId());
	}

	public static void removeCaseID() {
//		System.err.println("removeCaseID " + Thread.currentThread().getId());
	    caseIdMap.remove(Thread.currentThread().getId());
	}
	
	
/*============================================================================================*/
	
	public static synchronized ExtentReports getExecutionReport() {
//		System.err.println("getExecutionReport : " + Thread.currentThread().getId() );
		return executionReport.get();
	}

	public static synchronized void setExecutionReport(ExtentReports obj) {
//		System.err.println("setExecutionReport : " + Thread.currentThread().getId() );
		executionReport.set(obj);
	}
	
	public static void removeExecutionReport() {
		executionReport.remove();
	}
	
/*============================================================================================*/
	// Methods for managing PDF report object

	/**
	 * Retrieves the PDFReportObject instance associated with the current thread.
	 * 
	 * @return the PDFReportObject instance
	 */
	public static synchronized PDFReportObject getPDFReportObj() {
		return pdfReportObj.get();
	}
	
	/**
	 * Associates a PDFReportObject instance with the current thread.
	 * 
	 * @param obj the PDFReportObject instance to be set
	 */
	public static synchronized void setPDFReportObj(PDFReportObject obj) {
		pdfReportObj.set(obj);
	}
	
	/**
	 * Removes the association of the PDFReportObject instance from the current thread.
	 */
	public static void removePDFReportObj() {
		pdfReportObj.remove();
	}
	
/*============================================================================================*/
	// Methods for managing isScreen flag

	/**
	 * Retrieves the value of the isScreen flag associated with the current thread.
	 * 
	 * @return the value of the isScreen flag
	 */
	public static synchronized boolean getIsScreen() {
		return isScreen.get();
	}

	/**
	 * Sets the value of the isScreen flag for the current thread.
	 * 
	 * @param isScreenFlag the value to be set for the isScreen flag
	 */
	public static synchronized void setIsScreen(Boolean isScreenFlag) {
		isScreen.set(isScreenFlag);
	}
	
	/**
	 * Removes the association of the isScreen flag from the current thread.
	 */
	public static void removeisScreen() {
		isScreen.remove();
	}
	
/*============================================================================================*/
	
	// Methods for managing PlaywrightDriver instance

	public static PlaywrightDriver getDriverRef() {
		setIsScreen(false);
		return driverRef.get();
	}

	public static synchronized void setDriverRef(PlaywrightDriver driverRefParm) {
		driverRef.set(driverRefParm);
	}

	public static void removeDriverRef() {
		driverRef.remove();
	}

/*============================================================================================*/
	// Methods for managing custFlagForPDF flag

	/**
	 * Retrieves the value of the custFlagForPDF flag associated with the current thread.
	 * 
	 * @return the value of the custFlagForPDF flag
	 */
	public static synchronized boolean getCustFlagRef() {
		return custFlagForPDF.get();
	}

	/**
	 * Sets the value of the custFlagForPDF flag for the current thread.
	 * 
	 * @param custFlagForPDFRef the value to be set for the custFlagForPDF flag
	 */
	public static synchronized void setCustFlagRef(boolean custFlagForPDFRef) {
		custFlagForPDF.set(custFlagForPDFRef);
	}
	
	/**
	 * Removes the association of the custFlagForPDF flag from the current thread.
	 */
	public static void removeCustFlagRef() {
		custFlagForPDF.remove();
	}
	
/*============================================================================================*/
	// Methods for managing artifact objects

	/**
	 * Retrieves the List of ArtefactObject instances associated with the current thread.
	 * 
	 * @return the List of ArtefactObject instances
	 */
	public static synchronized List<ArtefactObject> getArtefactRef() {
		return artefactObj.get();
	}

	/**
	 * Sets the List of ArtefactObject instances for the current thread.
	 * 
	 * @param artefactRef the List of ArtefactObject instances to be set
	 */
	public static synchronized void setArtefactRef(List<ArtefactObject> artefactRef) {
		artefactObj.set(artefactRef);
	}
	
	/**
	 * Removes the association of the List of ArtefactObject instances from the current thread.
	 */
	public static void removeArtefactRef() {
		artefactObj.remove();
	}
/*============================================================================================*/
	// Methods for managing Sikuli Screen reference

	/**
	 * Retrieves the Sikuli Screen reference associated with the current thread.
	 * 
	 * @return the Sikuli Screen reference
	 */
	public static synchronized Screen getScreenRef() {
		return sikuli.get();
	}
	
	/**
	 * Sets the Sikuli Screen reference for the current thread.
	 * 
	 * @param screenRef the Sikuli Screen reference to be set
	 */

	public static synchronized void setScreenRef(Screen screenRef) {
		sikuli.set(screenRef);
	}
	
	/**
	 * Removes the association of the Sikuli Screen reference from the current thread.
	 */
	
	public static void removeScreenRef() {
		sikuli.remove();
	}

/*============================================================================================*/	
	
	// Methods for managing screenshot objects

	/**
	 * Retrieves the List of List of String representing the screenshot objects associated with the current thread.
	 * 
	 * @return the List of List of String representing the screenshot objects
	 */
	
	
/*============================================================================================*/
	
	public static synchronized List<List<String>> getSSObjRef() {
		return ssObj.get();
	}
	
	/**
	 * Sets the List of List of String representing the screenshot objects for the current thread.
	 * 
	 * @param ssObjRef the List of List of String representing the screenshot objects to be set
	 */

	public static synchronized void setSSObjRef(List<List<String>> ssObjRef) {
		ssObj.set(ssObjRef);
	}
	
	/**
	 * Removes the association of the List of List of String representing the screenshot objects from the current thread.
	 */
	
	public static synchronized void removeSSObjRef() {
		ssObj.get().clear();
		ssObj.remove();
	}
	
/*============================================================================================*/
	

	
	// Methods for managing data folder path

	/**
	 * Retrieves the data folder path associated with the current thread.
	 * 
	 * @return the data folder path
	 */
	public static synchronized String getDataFolder() {
		return dataFolder.get();
	}

	/**
	 * Sets the data folder path for the current thread.
	 * 
	 * @param dataFolderParm the data folder path to be set
	 */
	public static synchronized void setDataFolder(String dataFolderParm) {
		dataFolder.set(dataFolderParm);
	}

	/**
	 * Removes the association of the data folder path from the current thread.
	 */
	public static synchronized void removeDataFolder() {
		dataFolder.remove();
	}
	
	
/*============================================================================================*/
	
	// Methods for managing ITestContext instance

	/**
	 * Retrieves the ITestContext instance associated with the current thread.
	 * 
	 * @return the ITestContext instance
	 */
	public static ITestContext getITestContext() {
		return testContext.get();
	}
	
	/**
	 * Sets the ITestContext instance for the current thread.
	 * 
	 * @param testContextParam the ITestContext instance to be set
	 */
	
	public static void setITestContext(ITestContext testContextParam) {
		testContext.set(testContextParam);
	}
	
	/**
	 * Removes the association of the ITestContext instance from the current thread.
	 */
	
	
	public static void removeITestContext() {
		 testContext.remove();
	}
	
	// Methods for managing ExtentTest instance

	/**
	 * Sets the ExtentTest instance for the current thread.
	 * 
	 * @param extentTestParam the ExtentTest instance to be set
	 */

/*============================================================================================*/
	
	public static synchronized void setExtent(ExtentTest extentTestParam) {
		extentTest.set(extentTestParam);
	}
	
	/**
	 * Retrieves the ExtentTest instance associated with the current thread.
	 * 
	 * @return the ExtentTest instance
	 */
	
	public static synchronized ExtentTest getExtent() {
		return extentTest.get();
	}
	
	/**
	 * Removes the association of the ExtentTest instance from the current thread.
	 */
	
	public static void removeExtent() {
		extentTest.remove();
	}
	
	/**
	 * Kills the driver services by executing the command to kill the chromedriver.exe process.
	 */

/*============================================================================================*/
	
	public static void killDriverServices() {
		try {
			Runtime.getRuntime().exec("cmd /k taskkill /F /IM chromedriver.exe /T").waitFor(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Methods for managing temporary directory path

	/**
	 * Retrieves the temporary directory path associated with the current thread.
	 * 
	 * @return the temporary directory path
	 */
	
/*============================================================================================*/
	
	public static synchronized String getTempDirectoryPath() {
		return tempDirectoryPath.get();
	}
	
	/**
	 * Sets the temporary directory path for the current thread.
	 * 
	 * @param tempDirectoryPathParam the temporary directory path to be set
	 */

	public static synchronized void setTempDirectoryPath(String tempDirectoryPathParam) {
		ThreadUtils.tempDirectoryPath.set(tempDirectoryPathParam);
	}
	
	/**
	 * Removes the association of the temporary directory path from the current thread.
	 */
	
	public static synchronized void removeTempDirectoryPath(){
		
		try {
			FileUtils.deleteDirectory(new File(getTempDirectoryPath()));
			new File(getTempDirectoryPath()).delete();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		tempDirectoryPath.remove();
	}
	
	
	
/*============================================================================================*/

	/**
	* Retrieves the directory path for log files associated with the current thread.
	* 
	* @return the log directory path
	*/
	public static synchronized String getLogDirectoryPath() {
	return tempDirectoryPath.get() + "log";
	}
	
	/**
	* Retrieves the directory path for downloaded files associated with the current thread.
	* 
	* @return the download directory path
	*/
	public static String getDownloadDirectoryPath() {
		String testCaseName = substringAfterLast(Reporter.getCurrentTestResult().getTestClass().getXmlClass().getName().trim(), ".");
		return getTempDirectoryPath() + testCaseName + File.separator + "download";
	}
	
	/**
	* Retrieves the directory path for screenshots associated with the current thread.
	* 
	* @return the screenshot directory path
	*/
	public static synchronized String getScreenshotDirectoryPath() {
		return screenshotDirectoryPath .get() ;
	}
	
	public static synchronized void setScreenshotDirectoryPath(String path) {
		screenshotDirectoryPath.set(path) ;
	}
	
	public static synchronized void removeScreenshotDirectoryPath() {
		screenshotDirectoryPath.remove();
	}
	
	/**
	* Retrieves the directory path for artifacts associated with the current thread.
	* 
	* @return the artifact directory path
	*/
	public static String getArtefactDirectoryPath() {
	return tempDirectoryPath.get() + "artefact";
	}
	
/*============================================================================================*/
	
	// Methods for managing logger instance

	/**
	 * Retrieves the logger instance associated with the current thread.
	 * 
	 * @return the logger instance
	 */
	public static synchronized Logger getLogger() {
		return logger.get();
	}
	
	/**
	 * Removes the association of the logger instance from the current thread.
	 */
	
	public static synchronized void removeLogger() {
//		logger.get().removeAllAppenders();
		logger.remove();
	}
	
	/**
	 * Sets the logger instance for the current thread.
	 * 
	 * @param loggerParam the logger instance to be set
	 */

	public static synchronized void setLogger(Logger loggerParam) {
		logger.set(loggerParam);
	}
	
/*============================================================================================*/
	
	/**
	* Removes the PDF directory path associated with the current thread.
	*/
	public static synchronized void removePDFDirectoryPath() {
		pdfReportPath.remove();
	}
	
	/**
	* Removes the PDF folder and its contents associated with the current thread.
	* This method deletes the directory and all its subdirectories and files.
	*/
	public static synchronized void removePDFFolder() {
		try {
			FileUtils.deleteDirectory(new File(pdfReportPath.get()));
		} catch (IOException e) {
			BaseClass.log().warn(e.getMessage());
		}
	}


//////////////////////////////////////////////////////////////////////////
	
	// Methods for managing extentReportPath

	/**
	 * Retrieves the extent report path associated with the current thread.
	 * 
	 * @return the extent report path
	 */
	public static synchronized String getExtentReportPath() {
		return extentReportPath.get();
	}
	
	/**
	 * Sets the extent report path for the current thread.
	 * 
	 * @param extentReportPathParm the extent report path to be set
	 */

	public static synchronized void setExtentReportPath(String extentReportPathParm) {
		extentReportPath.set(extentReportPathParm);
	}
	
	/**
	 * Removes the association of the extent report path from the current thread.
	 */
	
	public static synchronized void removeExtentReportPath() {
		extentReportPath.remove();
	}
//////////////////////////////////////////////////////////////////////////
	
	// Methods for managing pdfReportPath

	/**
	 * Retrieves the PDF report path associated with the current thread.
	 * 
	 * @return the PDF report path
	 */
	public static synchronized String getPDFReportPath() {
		return pdfReportPath.get();
	}
	
	/**
	 * Sets the PDF report path for the current thread.
	 * 
	 * @param pdfReportPathParm the PDF report path to be set
	 */
	public static synchronized void setPDFReportPath(String pdfReportPathParm) {
		pdfReportPath.set(pdfReportPathParm);
	}
	
	/**
	 * Removes the association of the PDF report path from the current thread.
	 */
	public static void removePDFReportPath() {
		pdfReportPath.remove();
	}
	
/////////////////////////////////////////////////////////////////////////////
	

	 /**
	 * Retrieves the Failure SS Base64 String associated with the current thread.
	 * 
	 * @return the PDF report path
	 */
	 public static synchronized String getFailureSSBase64() {
	 	return failureSSBase64.get();
	 }
	 
	 /**
	 * Sets the Failure SS Base64 String for the current thread.
	 * 
	 * @param pdfReportPathParm the PDF report path to be set
	 */
	 public static synchronized void setFailureSSBase64(String failureSSBase64Param) {
	 	failureSSBase64.set(failureSSBase64Param);
	 }
	 
	 /**
	 * Removes the association of the Failure SS Base64 String from the current thread.
	 */
	 public static void removeFailureSSBase64() {
	 	failureSSBase64.remove();
	 }
	 
/////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Contains all remove methods.
	 */
	public static void removeMethods(){
		ThreadUtils.removeisScreen();
		ThreadUtils.removeExtent();
		ThreadUtils.removeLogger();
		ThreadUtils.removeScreenRef();
		ThreadUtils.removeSSObjRef();
		ThreadUtils.removeTempDirectoryPath();
		ThreadUtils.removeDataFolder();
		ThreadUtils.removeDriverRef();
		ThreadUtils.removeCustFlagRef();
		ThreadUtils.removeArtefactRef();
		ThreadUtils.removeScreenshotDirectoryPath();
		ThreadUtils.removeFailureSSBase64();
//		ThreadUtils.removeExtentReportPath();
//		ThreadUtils.removeTestDescription();
		if(BaseClass.getParameter("Generate_TestResultsDoc", "No").equalsIgnoreCase("yes") ||
				BaseClass.getParameter("generateDocument", "false").trim().toLowerCase().matches("pdf|docx")) {
//			ThreadUtils.removePDFFolder();
			ThreadUtils.removePDFDirectoryPath();
		}
	}
	
}