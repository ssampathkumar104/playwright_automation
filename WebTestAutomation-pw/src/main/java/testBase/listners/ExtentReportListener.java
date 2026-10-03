package testBase.listners;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.IConfigurationListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import testBase.BaseClass;
import testBase.BrowserFactory;
import testBase.ThreadUtils;
import testReportingAPI.dto.TestCaseRequest;
import testReportingAPI.dto.TestCaseUpdateRequest;
import testReportingAPI.dto.TestScriptRequest;
import testReportingAPI.dto.TestSuiteRequest;
import testReportingAPI.dto.TestSuiteUpdateRequest;

import htmlSummary.functions.GenAIReportFunctions;
import htmlSummary.config.SummaryConfigLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("all")
public final class ExtentReportListener extends ExtentReportSupportMethods
		implements IConfigurationListener, ITestListener, ISuiteListener, IReporter {

	private static final Logger summaryLog = LogManager.getLogger(ExtentReportListener.class);
	static ExtentReports er;
	static Long suiteID;
	static String htmlDate;
	static List<String> dirPath = new ArrayList() ;
	
	// API End points for updating test details in db
	// Replace with server url
    private static final String API_URL = "http://localhost:9515/api/test-suites";
    private static final String CASE_API_URL = "http://localhost:9515/api/test-cases";
    private static final String SCRIPT_API_URL = "http://localhost:9515/api/test-scripts";

    /** Prints a GenAI Summary log line via System.out (not suppressed by the OFF root logger). */
    private static void genAiLog(String message) {
        System.out.println("[GenAI Summary] " + message);
    }
	
	@Override
	public void onStart(ISuite suite) {
		
		String sFileName = FilenameUtils.getBaseName(suite.getXmlSuite().getFileName());
		String sName= suite.getXmlSuite().getTests().get(0).getClasses().get(0).getName();
		String suiteName = sFileName.equalsIgnoreCase("testng-customsuite")
					? sName.substring(sName.lastIndexOf(".") + 1)
					: sFileName;
				
		htmlDate = Objects.isNull(htmlDate)
					? LocalDateTime.now().
						format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss")).replace(":", "-")
					: htmlDate;
		try {
			String reportName = new String(suiteName.trim() + "_" + htmlDate);
			er = ExtentReportNG.setupExtentReport(reportName);
			ThreadUtils.setExecutionReport(er);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//***********************//
		
		// Creating data into TestSuiteRequest object and sending data to db
		String sysValue = System.getProperty("publishToDB");
		String planValue = suite.getXmlSuite().getParameter("publishToDB");
		
		Boolean flag = Boolean.parseBoolean(isNotBlank(sysValue) ? sysValue : planValue);
		 try {
            TestSuiteRequest testSuiteRequest = new TestSuiteRequest();
            // Extract details from pom.xml
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("pom.xml"));

            String groupName = document.getDocumentElement().getElementsByTagName("groupId").item(0).getTextContent();
            String project = document.getDocumentElement().getElementsByTagName("artifactId").item(0).getTextContent();

            testSuiteRequest.setGroupName(groupName);
            testSuiteRequest.setProject(project);
            XmlSuite xmlSuite = suite.getXmlSuite();
            testSuiteRequest.setSuite(xmlSuite.getName());
            testSuiteRequest.setSuiteStatus("In Progress");
            
//            String env = "";
//            try {
//            	String[] parts = xmlSuite.getParameter("BASE_URL").split("/");
//            	env = parts[parts.length - (parts[parts.length - 1].matches("[0-9a-fA-F-]{36}") ? 2 : 1)];
//            }catch (Exception e) {
//				// TODO: handle exception
//			}
            
            String env = "";
            try {
            	env = xmlSuite.getParameter("BASE_URL").split("/")[xmlSuite.getParameter("BASE_URL").split("/").length - 1];
            }catch (Exception e) {
				// TODO: handle exception
			}
            testSuiteRequest.setEnvironment(env.contains(".com") ? null : env);
            // null or from jenkins parameter
            testSuiteRequest.setReleaseVersion(null);
            testSuiteRequest.setSuiteStartDate(OffsetDateTime.now(ZoneOffset.UTC));

            suiteID = sendToAPI(testSuiteRequest, API_URL, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void onFinish(ISuite suite) {
		String sysValue = System.getProperty("publishToDB");
		String planValue = suite.getXmlSuite().getParameter("publishToDB");

		Boolean flag = Boolean.parseBoolean(isNotBlank(sysValue) ? sysValue : planValue);
		
		// Updating Test Suite data in db
        try {
            TestSuiteUpdateRequest suiteUpdateRequest = new TestSuiteUpdateRequest();
            
            boolean hasFailure = suite.getResults().values().stream()
            	    .anyMatch(result -> result.getTestContext().getFailedTests().size() > 0);

            boolean hasPass = suite.getResults().values().stream()
            	    .anyMatch(result -> result.getTestContext().getPassedTests().size() > 0);

            boolean hasSkipOnly = suite.getResults().values().stream()
            	    .allMatch(result -> 
            	        result.getTestContext().getSkippedTests().size() > 0 &&
            	        result.getTestContext().getPassedTests().size() == 0 &&
            	        result.getTestContext().getFailedTests().size() == 0
            	    );
            String status = hasFailure ? "FAIL" : (hasSkipOnly ? "SKIP" : "PASS");
			suiteUpdateRequest.setSuiteStatus(status);
			suiteUpdateRequest.setSuiteEndDate(OffsetDateTime.now(ZoneOffset.UTC));

            updateAPI(suiteUpdateRequest, API_URL + "/" + suiteID, flag);
            
            ThreadUtils.removeCaseID();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		//***********************//
		
		try {
			ThreadUtils.getExecutionReport().flush();
		}catch (NullPointerException e) {
			er.flush();
		}
		LogFormatter.closeAllAppenders();
		dirPath.forEach(s -> {
			try {
				FileUtils.forceDeleteOnExit(new File(s));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});;
		
		// Generate GenAI HTML summary — reads generateSummary from the suite.
		try {
			String gsSys  = System.getProperty("generateSummary");
			String gsPlan = suite.getXmlSuite().getParameter("generateSummary");
			boolean generateSummary = Boolean.parseBoolean(isNotBlank(gsSys) ? gsSys : gsPlan);

			genAiLog("generateSummary = " + generateSummary);

			if (generateSummary) {
				genAiLog("env file      : " + (SummaryConfigLoader.isEnvFileLoaded()
						? SummaryConfigLoader.getLoadedEnvFilePath() : "NOT FOUND (add genai-summary.env to project root)"));
				genAiLog("prompt folder : " + SummaryConfigLoader.getPromptFolderAbsolutePath()
						+ (SummaryConfigLoader.isPromptFolderAvailable() ? "" : "  [MISSING]"));
				genAiLog("output folder : " + SummaryConfigLoader.getOutputFolderAbsolutePath());

				String reportPath = ThreadUtils.getExtentReportPath();
				if (!SummaryConfigLoader.hasRequiredCredentials()) {
					genAiLog("SKIPPED: credentials missing — add them to genai-summary.env "
							+ "(BEARER_BASE_URI, CLIENT_ID, CLIENT_SECRET_ID, TOKEN_GENERATION_USERNAME, "
							+ "TOKEN_GENERATION_PASSWORD, XTENANT_ID, RESOURCE_BASE_URL) or pass as -D/plan params");
				} else if (!SummaryConfigLoader.arePromptsAvailable()) {
					genAiLog("SKIPPED: prompt templates missing — add functionalSummaryPrompt.txt and "
							+ "e2eFunctionalSummaryPrompt.txt to the '" + SummaryConfigLoader.getPromptFolder()
							+ "' folder in your project root");
				} else if (reportPath == null || reportPath.isEmpty()) {
					genAiLog("SKIPPED: extent report path is null/empty");
				} else {
					genAiLog("generating summary for: " + reportPath);
					GenAIReportFunctions.generateSummaryForReport(reportPath);
					genAiLog("summary generated successfully");
				}
			}
		} catch (Exception e) {
			genAiLog("WARNING: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Call the upload to sharepoint method
//		SharepointActions.uploadHTMLToSharePoint();
//		ThreadUtils.killDriverServices();
//		try {
//			ExtentReportNG.sendEmailWithAttachment(suite);
//		} catch (EmailException e) {
//			e.printStackTrace();
//		}
	}

	/*
	 * =============================================================================
	 */
	

	public void onStart(ITestContext context){
		try {
			ThreadUtils.setITestContext(context);
		
			boolean isGenerateDocuemt_True = BaseClass.getParameter("generateDocument", "false").equalsIgnoreCase("docx|pdf");
			boolean isGenerate_TestResultsDoc_Yes = BaseClass.getParameter("Generate_TestResultsDoc", "no").equalsIgnoreCase("yes");
			
			// Get the sharepoint_url parameter from testplan
			String sharePointURL = BaseClass.getParameter("sharePointURL", "");
			
			// Update the sharepoint url in json file
	//		SharepointActions.updateSharepointJSONFile(sharePointURL);
			
			if (isGenerateDocuemt_True && isGenerate_TestResultsDoc_Yes) {
				System.err.println(
						"Do NOT include parameters 'generateDocument' : 'true' and 'Generate_TestResultsDoc' : 'yes' at a time in the test plan. You need to include any one of those parameters but NOT both.");
				assert (false);
			}
	
			String testName = context.getCurrentXmlTest().getXmlClasses().get(0).getName().trim();
			String testName1 = context.getName().trim().equalsIgnoreCase("Default Test")
					? testName.substring(testName.lastIndexOf(".") + 1)
					: context.getName().trim();
			
			ExtentTest test = null;
			try {
				test = ThreadUtils.getExecutionReport().createTest(new String(testName1));
			}catch (NullPointerException e) {
				test = er.createTest(new String(testName1));
			}
			ThreadUtils.setExtent(test);
			createTempDirectory();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//***********************//
		
		// Creating data into TestCaseRequest object and sending data to db
        try {
            TestCaseRequest testCaseRequest = new TestCaseRequest();
            testCaseRequest.setSuiteId(suiteID);
            testCaseRequest.setTestCase(context.getName());
            testCaseRequest.setTestStartTime(context.getStartDate().toInstant().atOffset(ZoneOffset.UTC));
            testCaseRequest.setRunStatus("In Progress");

			ThreadUtils.setCaseID(sendToAPI(testCaseRequest, CASE_API_URL + "?suiteId=" + suiteID, Boolean.parseBoolean(BaseClass.getParameter("publishToDB","false"))));
			ThreadUtils.setSuiteID(suiteID);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public void onFinish(ITestContext context) {
		// Updating Test Case data in db
		String caseStatus;
        if (context.getFailedTests().size() > 0) {
        	caseStatus = "FAIL";
        } else if (context.getSkippedTests().size() > 0) {
        	caseStatus = "SKIP";
        } else {
        	caseStatus = "PASS";
        }
		
		try {
            TestCaseUpdateRequest caseUpdateRequest = new TestCaseUpdateRequest();
            caseUpdateRequest.setTestEndTime(OffsetDateTime.now(ZoneOffset.UTC));
            caseUpdateRequest.setRunStatus(caseStatus);
            // failure reason at test case level is not needed, think again and remove it later
            caseUpdateRequest.setFailureReason(context.getFailedTests().size() > 0 ? context.getFailedTests().getAllResults().iterator().next().getThrowable().getMessage() : "N/A");
            updateAPI(caseUpdateRequest, CASE_API_URL + "/" + ThreadUtils.getCaseID() + "?suiteId=" + suiteID, Boolean.parseBoolean(BaseClass.getParameter("publishToDB","false")));
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		//***********************//
		
		try {
			for (XmlClass c :  context.getCurrentXmlTest().getClasses()) {
				attachArtefactsToReport(substringAfterLast(c.getName().trim(), "."));
			}
			dirPath.add(ThreadUtils.getTempDirectoryPath());
			ThreadUtils.removeMethods();
		} catch (NullPointerException e) {
			System.err.println(">>>>>>>>>>>>>>> reports/logs/screenshots are not available - NullPointerException <<<<<<<<<<<<<");
			e.printStackTrace();
			try {
				ThreadUtils.getExecutionReport().removeTest(context.getName().trim());
			}catch (NullPointerException nullExp) {
				er.removeTest(context.getName().trim());
			}
			er.removeTest(context.getName().trim());
		} catch (IOException e) {
			System.err.println(">>>>>>>>>>>>>>> " + e.getMessage() + "<<<<<<<<<<<<<");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println(">>>>>>>>>>>>>>> reports/logs/screenshots are not available <<<<<<<<<<<<<");
			System.err.println(">>>>>>>>>>>>>>> " + e.getMessage() + "<<<<<<<<<<<<<");
			e.printStackTrace();
		}
		BrowserFactory.closeBrowser();
	}

	/*
	 * =============================================================================
	 */

	public void onTestStart(ITestResult result) {
		try {
			TestScriptRequest testScriptRequest = new TestScriptRequest();
			
//			String fullClassName = result.getTestClass().getXmlClass().getName();
			
			 // Access the ITestResult directly from the parameter
	        String methodName = result.getMethod().getMethodName();
	        String className = result.getTestClass().getName(); // This line was causing the NullPointerException
	        String description = result.getMethod().getDescription();
	        
	        String testMethodClassName = className.substring(className.lastIndexOf('.') + 1);
	        
			testScriptRequest.setTestCaseId(ThreadUtils.getCaseID());
			testScriptRequest.setScript(testMethodClassName);
			testScriptRequest.setScriptStartTime(OffsetDateTime.now(ZoneOffset.UTC));
			testScriptRequest.setRunStatus("In Progress");
			testScriptRequest.setIsDependency(false);
			ThreadUtils.setScriptID(sendToAPI(testScriptRequest, 
											SCRIPT_API_URL + "?testCaseId=" + ThreadUtils.getCaseID(), 
											Boolean.parseBoolean(BaseClass.getParameter("publishToDB","false"))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	public void onTestFailedWithTimeout(ITestResult result) {
	}

	public void onTestSuccess(ITestResult result) {
		String logText = "<b>Test Method: " + result.getMethod().getMethodName() + " is Passed.</b>";
		Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREEN);
		ThreadUtils.getExtent().log(Status.PASS, m);
		ThreadUtils.getPDFReportObj().setResult(result);
	}

	public void onTestSkipped(ITestResult result) {
		String logText = "<b>Test Method: " + result.getMethod().getMethodName() + " is Skipped.</b>";
		Markup m = MarkupHelper.createLabel(logText, ExtentColor.ORANGE);
		ThreadUtils.getExtent().log(Status.SKIP, m);
		handleFailure(result, ExtentColor.ORANGE.toString());
		ThreadUtils.getPDFReportObj().setResult(result);
	}

	public void onTestFailure(ITestResult result) {
		String logText = "<b>Test Method: " + result.getMethod().getMethodName() + " is Failed.</b>";
		Markup m = MarkupHelper.createLabel(logText, ExtentColor.RED);
		ThreadUtils.getExtent().log(Status.FAIL, m);
		handleFailure(result, ExtentColor.RED.toString());
		ThreadUtils.getPDFReportObj().setResult(result);
	}

	/*
	 * =============================================================================
	 */

	@Override
	public void onConfigurationSuccess(ITestResult result) {
		String logText = "<b>Test Method: "+result.getMethod().getMethodName() + " is Passed.</b>";
//		Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREEN);
//		ThreadUtils.getExtent().log(Status.PASS, m);
	}

	@Override
	public void onConfigurationFailure(ITestResult result) {
//		if(!Objects.isNull(result.getThrowable())) {
//			String logText = "<b>Configuration Method: " + result.getMethod().getMethodName() + " is Failed.</b>";
//			Markup m = MarkupHelper.createLabel(logText, ExtentColor.RED);
//			ThreadUtils.getExtent().log(Status.FAIL, m);
//			
//			handleFailure(result, ExtentColor.RED.toString());
//		}
	}

	@Override
	public void onConfigurationSkip(ITestResult result) {
		if (!Objects.isNull(result.getThrowable())) {
			String logText = "<b>Configuration Method: " + result.getMethod().getMethodName() + " is Skipped.</b>";
			Markup m = MarkupHelper.createLabel(logText, ExtentColor.ORANGE);
			ThreadUtils.getExtent().log(Status.SKIP, m);
			handleFailure(result, ExtentColor.ORANGE.toString());
			ThreadUtils.getPDFReportObj().setResult(result);
		}
	}

	/*
	 * =============================================================================
	 */

//	private static final String ROW_TEMPLATE = "<tr class=\"%s\"><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
	 private static final String ROW_TEMPLATE = "%s,%s,%s,%s,%s,%s,%d\n";
	 private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
//		String reportTemplate = initReportTemplate();
		String csvFileName =	FilenameUtils.getBaseName(xmlSuites.get(0).getFileName());
		
		final String body = suites.stream().flatMap(suiteToResults()).collect(Collectors.joining());
		
		String reportDir = System.getProperty("user.dir") + File.separator + "Reports" + File.separator ;
		File f = new File(reportDir + csvFileName + "_" + htmlDate + ".csv");
		try {
			FileUtils.writeStringToFile(f, "Report,Test Case,Test Method,Status,Start Time,End Time,Execution Time(ms)\n" + body);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		saveReportTemplate(outputDirectory, reportTemplate.replaceFirst("</tbody>", String.format("%s</tbody>", body)));
	}

	private Function<ISuite, Stream<? extends String>> suiteToResults() {
		return suite -> suite.getResults().entrySet().stream().flatMap(resultsToRows(suite));
	}

	private Function<Map.Entry<String, ISuiteResult>, Stream<? extends String>> resultsToRows(ISuite suite) {
		return e -> {
			ITestContext testContext = e.getValue().getTestContext();
			Set<ITestResult> failedTests = testContext.getFailedTests().getAllResults();
			Set<ITestResult> passedTests = testContext.getPassedTests().getAllResults();
			Set<ITestResult> skippedTests = testContext.getSkippedTests().getAllResults();

			String suiteName = suite.getName();
			return Stream.of(failedTests, passedTests, skippedTests)
					.flatMap(results -> generateReportRows(e.getKey(), suiteName, results).stream());
		};
	}

	private List<String> generateReportRows(String testName, String suiteName, Set<ITestResult> allTestResults) {
		return allTestResults.stream().map(testResultToResultRow(testName, suiteName)).collect(toList());
	}

	private Function<ITestResult, String> testResultToResultRow(String testName, String suiteName) {
		return testResult -> {
			String sn = suiteName + "_" + htmlDate;
			long startTimeMillis = testResult.getStartMillis();
            long endTimeMillis = testResult.getEndMillis();
            long executionTime = endTimeMillis - startTimeMillis;

            String startTime = formatTimestamp(startTimeMillis);
            String endTime = formatTimestamp(endTimeMillis);
            
            String status;
			switch (testResult.getStatus()) {
				 case ITestResult.FAILURE:
	                 status = "FAILED";
	                 break;
	             case ITestResult.SUCCESS:
	                 status = "PASSED";
	                 break;
	             case ITestResult.SKIP:
	                 status = "SKIPPED";
	                 break;
	             default:
	                 status = "UNKNOWN";
	        }

        return String.format(ROW_TEMPLATE, sn, testName.trim(), testResult.getName(), status, startTime, endTime, executionTime);
		};
	}
	
	private void attachArtefactsToReport(String name) throws NullPointerException, IOException {
		StringBuilder reportContentBuilder = new StringBuilder();

		String log = attachLogFilesToReport(name);
		if (log != null) {
			reportContentBuilder.append(log);
		}

		String artefact = attachArtefactFileToReport(name);
		if (artefact != null) {
			reportContentBuilder.append(artefact);
		}

		String screenshot = attachScreenShotsToReport(name);
		if (screenshot != null) {
			reportContentBuilder.append(screenshot);
		}

		// Log the combined report content
		ThreadUtils.getExtent().info(reportContentBuilder.toString());

	}
	
	private String formatTimestamp(long millis) {
        return TIMESTAMP_FORMAT.format(new Date(millis));
    }
	
	/**
	 * This Method to create temp directory
	 *
	 */
	private static String generateRandomID24() {
        // Create a secure random byte array of length 16
        byte[] randomBytes = new byte[16]; // 128 bits
        new SecureRandom().nextBytes(randomBytes);
        
        // Encode bytes to Base64 and remove padding
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
	
	private static void createTempDirectory() {
		String str = System.getProperty("user.dir") + File.separator + "artefact" + File.separator + "temp" + File.separator;
		String tempDirectoryPath = str + generateRandomID24() + File.separator;
		File fname = new File(tempDirectoryPath);
		if (!fname.exists())
			fname.mkdirs();
		ThreadUtils.setTempDirectoryPath(tempDirectoryPath);
	}

//	private String initReportTemplate() {
//		String template = null;
//		byte[] reportTemplate;
//		try {
//			String reportDir = System.getProperty("user.dir") + File.separator + "Reports" ;
//			reportTemplate = Files.readAllBytes(Paths.get(System.getProperty("user.dir")+"/src/main/java/reportTemplate.html"));
//			template = new String(reportTemplate, "UTF-8");
//		} catch (IOException e) {
//			System.err.println("Problem initializing template : " + e);
//		}
//		return template;
//	}
//
//	private void saveReportTemplate(String outputDirectory, String reportTemplate) {
//		new File(outputDirectory).mkdirs();
//		try {
//			String reportDir = System.getProperty("user.dir") + File.separator + "Reports" ;
//			PrintWriter reportWriter = new PrintWriter(
//					new BufferedWriter(new FileWriter(new File(reportDir, "temp_" + reportName + ".csv"))));
//			reportWriter.println(reportTemplate);
//			reportWriter.flush();
//			reportWriter.close();
//		} catch (IOException e) {
//			System.err.println("Problem initializing template : " + e);
//		}
//	}

}
