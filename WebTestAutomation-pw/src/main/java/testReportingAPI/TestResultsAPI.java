package testReportingAPI;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import testBase.BaseClass;
import testBase.ThreadUtils;
import testReportingAPI.dto.TestScriptRequest;

public class TestResultsAPI extends APILoggerConfig implements ITestListener  {

	private ObjectMapper objectMapper;
	
	public TestResultsAPI() {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	private static final String SCRIPT_API_URL = "http://localhost:9515/api/test-scripts";
	private static final String SCREENSHOT_API_URL = "http://localhost:9515/api/test-screenshots";
    
	/**
	 * Sending Test Script data to TestScripts db table
	 * @param result
	 */
	public void processTestScript(ITestResult result) {
		if(Boolean.parseBoolean(BaseClass.getParameter("publishToDB","false"))){
			try {
				log("TestResultsAPI - Processing test script for publishing to database");
				TestScriptRequest testScriptRequest = new TestScriptRequest();
				
				String testCaseName = result.getTestContext().getCurrentXmlTest().getName();
				String fullClassName = result.getTestClass().getXmlClass().getName();
				
				String testMethodClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
				
//				testScriptRequest.setTestCaseId(ThreadUtils.getCaseID());
//				testScriptRequest.setScript(testMethodClassName);
//				testScriptRequest.setScriptStartTime(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(result.getStartMillis()), ZoneOffset.UTC));
				testScriptRequest.setScriptEndTime(OffsetDateTime.now(ZoneOffset.UTC));
	
				boolean isDependency = result.getTestContext().getCurrentXmlTest().getClasses().size() > 1;
				testScriptRequest.setIsDependency(isDependency);
	
				switch (result.getStatus()) {
					case ITestResult.SUCCESS:
						testScriptRequest.setRunStatus("PASS");
						break;
					case ITestResult.FAILURE:
						testScriptRequest.setRunStatus("FAIL");
						testScriptRequest.setFailureReason(result.getThrowable().getMessage());
						break;
					case ITestResult.SKIP:
						testScriptRequest.setRunStatus("SKIP");
						break;
					default:
						testScriptRequest.setRunStatus("UNKNOWN");
						break;
				}
	
				// Set suite and script IDs for S3 path construction
				log("TestResultsAPI - Suite Id: " + ThreadUtils.getSuiteID() + ", Case Id : " + ThreadUtils.getCaseID()+ ", Script Id: " + ThreadUtils.getScriptID());
				String key = "DB/" + ThreadUtils.getSuiteID() + "/" + testCaseName + "/" + testMethodClassName + "/" ;
				
				// Store Log files in  S3
				try {
					String logPath = testMethodClassName + File.separator + "log" ;
					String logFilePath = ThreadUtils.getTempDirectoryPath() + logPath + File.separator + testMethodClassName + "_detailedLog.txt";
					
					// Check if file exists before proceeding
					if (new File(logFilePath).exists()) {
						String logURI = new S3Service().uploadFile(key + "log/" + testMethodClassName + "_detailedLog.txt", logFilePath);
						testScriptRequest.setS3LogFile(logURI);
						log("TestResultsAPI - Successfully uploaded log file to S3: " + logURI);
					}
				} catch (Exception e) {
					logError("TestResultsAPI - Error uploading log file to S3: " + e.getMessage());
					getStackTraceAsString(e);
					testScriptRequest.setS3LogFile(null);
				}
				
				// Store Artifact files S3
				try {
					String artefactPath = testMethodClassName + File.separator + "artefact" ;
					String artefactFilePath = ThreadUtils.getTempDirectoryPath() + artefactPath + File.separator + testMethodClassName + ".docx";
					
					// Check if file exists before proceeding
					if (new File(artefactFilePath).exists()) {
						String artefactURI = new S3Service().uploadFile(key + "artefact/" + testMethodClassName + ".docx", artefactFilePath);
						testScriptRequest.setS3Artifact(artefactURI);
						log("TestResultsAPI - Successfully uploaded log file to S3: " + artefactURI);
					}
				} catch (Exception e) {
					logError("TestResultsAPI - Error uploading Artefact file to S3: " + e.getMessage());
					getStackTraceAsString(e);
					testScriptRequest.setS3Artifact(null);
				}
				
				// Get paths related files and directories
				String screenshotsPath = testMethodClassName + File.separator + "screenshot";
				String screenshotsDir = ThreadUtils.getTempDirectoryPath() + screenshotsPath;
				String pdfPath = screenshotsDir + File.separator + testMethodClassName + "_screenshots.pdf";
				
				// Get all JPG or PNG files
				File[] screenshotFiles = new File(screenshotsDir).listFiles((dir, name) -> 
					name.toLowerCase().endsWith(".jpg") || 
					name.toLowerCase().endsWith(".jpeg") || 
					name.toLowerCase().endsWith(".png"));
					
				if (screenshotFiles == null || screenshotFiles.length == 0) {
					log("TestResultsAPI - No screenshot files found in: " + screenshotsDir);
					testScriptRequest.setS3Screenshots(null);
				} else {
					// Log information about each screenshot found
					log("TestResultsAPI - Found " + screenshotFiles.length + " screenshots");
					createPDFDocumentWithScreenshots(pdfPath, screenshotFiles);
				}
				
				// Read combined PDF file. If not exists create a basic PDF as last resort
				File pdfFile = new File(pdfPath);
				if (!pdfFile.exists() || pdfFile.length() == 0) {
					log("TestResultsAPI - PDF file not created properly: " + pdfPath);
					try {
						generatePlaceholderPDF(screenshotsDir, testMethodClassName);
					} catch (Exception e) {
						logError("TestResultsAPI - Failed to create basic PDF: " + e.getMessage());
					}
				}
					
				// Upload to S3 if feature flag is enabled
				try {
					String screenshotsURI = new S3Service().uploadFile(key + "screenshot/" + testMethodClassName + "_screenshots.pdf", pdfPath);
					testScriptRequest.setS3Screenshots(screenshotsURI);
					log("TestResultsAPI - Successfully uploaded screenshot PDF to S3: " + screenshotsURI);
				} catch (Exception e) {
					logError("TestResultsAPI - Failed to upload screenshots to S3: " + e.getMessage());
					getStackTraceAsString(e);
					testScriptRequest.setS3Screenshots(null);
				}
					
				updateAPI(testScriptRequest, SCRIPT_API_URL + "/" + ThreadUtils.getScriptID(), Boolean.parseBoolean(BaseClass.getParameter("publishToDB","false")));
						
			} catch (Exception e) {
				System.out.println("$$$$ " + e.getMessage() + " $$$$");
				logError("TestResultsAPI - Exception " + e.getStackTrace());
			}
		}
	}
	
	/**
	 * POST API method to Create data in db tables
	 * @param <T>
	 * @param request
	 * @param url
	 * @return
	 * @throws Exception
	 */
	protected <T> Long sendToAPI(T request, String url, Boolean flag) throws Exception {
		if (flag) {
			String jsonPayload = objectMapper.writeValueAsString(request);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
					.build();
			HttpResponse<String> response = null;
			try {
				response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

				JsonNode jsonResponse = objectMapper.readTree(response.body());
				if (jsonResponse.has("id")) {
					log("====== INSERT : " + url + " ID: " + jsonResponse.get("id") + " ======");
					log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(jsonPayload))); 
					log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(response.body()))); 
					return jsonResponse.get("id").asLong();
				} else {
					throw new Exception("API response does not contain an 'id' field: " + response.body());
				}
			}
			catch (Exception e) {
				log("====== " + e.getMessage() + " ======");
				log("====== INSERT : " + url + " ======");
				log("====== INSERT JSON Payload ======");
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(jsonPayload))); 
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(response.body()))); 
				return (long) 0;
			} 
		}
		return (long) 0;
	}
	
	/**
	 * PUT API method to Update data in db tables
	 * @param <T>
	 * @param request
	 * @param url
	 * @throws Exception
	 */
	protected <T> void updateAPI(T request, String url, Boolean flag) throws Exception {
		if (flag) {
			String jsonPayload = objectMapper.writeValueAsString(request);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
					.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
					.build();
			HttpResponse<String> response = null;
			try {
				response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				log("====== UPDATE : " + url + " ======");
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(jsonPayload))); 
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(response.body())));
			}
			catch (Exception e) {
				log("====== " + e.getMessage() + " ======");
				log("====== UPDATE JSON Payload ======");
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(jsonPayload))); 
				log(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(response.body()))); 
			} 
		}
	}
	
	/**
	 * Creates a placeholder PDF with a text message when no screenshots are found
	 * @param directory Directory to save the PDF in
	 * @param testName Test name to include in filename
	 */
	private void generatePlaceholderPDF(String directory, String testName) throws IOException {
		String pdfPath = directory + File.separator + testName + "_screenshots.pdf";
		try (PDDocument document = new PDDocument()) {
			addTextPage(document, "No screenshots were found for this test execution.");
			document.save(pdfPath);
			log("TestResultsAPI - Created placeholder PDF: " + pdfPath);
		}
	}
	
	/**
	 * Adds a text page to a PDF document
	 * @param document PDF document to add page to
	 * @param message Message to add to the page
	 */
	private void addTextPage(PDDocument document, String message) throws IOException {
		PDPage page = new PDPage(PDRectangle.A4);
		document.addPage(page);
		
		try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
			contentStream.newLineAtOffset(100, 700);
			contentStream.showText("Test Execution Report");
			
			contentStream.setFont(PDType1Font.HELVETICA, 12);
			contentStream.newLineAtOffset(0, -30);
			
			for (String line : message.split("\n")) {
				contentStream.showText(line);
				contentStream.newLineAtOffset(0, -15);
			}
			
			contentStream.endText();
		}
	}
	
	/**
	 * Safely calculates scale factor for an image, avoiding divide-by-zero errors
	 * @param imageWidth Width of the image
	 * @param imageHeight Height of the image
	 * @param pageWidth Width of the page
	 * @param pageHeight Height of the page
	 * @return Safe scale factor
	 */
	private float calculateSafeScale(float imageWidth, float imageHeight, float pageWidth, float pageHeight) {
		// Handle invalid image dimensions
		if (imageWidth <= 0 || imageHeight <= 0) {
			ThreadUtils.getLogger().warn("Invalid image dimensions, using default scale of 1.0");
			return 1.0f;
		}
		
		float scale = Math.min(
			pageWidth / imageWidth,
			pageHeight / imageHeight
		) * 0.9f; // Use 90% of available space for margin
		
		// Handle overflow or extreme values
		if (Float.isNaN(scale) || Float.isInfinite(scale) || scale <= 0 || scale > 100) {
			ThreadUtils.getLogger().warn("Calculated scale is invalid: " + scale + ", using default");
			return 0.8f; // Reasonable default
		}
		
		return scale;
	}
	

	private PDImageXObject createImageXObjectSafely(File imageFile, PDDocument document) throws IOException {
	    try {
	        // First attempt - use content-based detection
	        return PDImageXObject.createFromFileByContent(imageFile, document);
	    } catch (Exception e) {
	        logWarn("Content-based image detection failed for: " + imageFile.getName() + ", falling back to extension-based detection");
	        // If that fails, try extension-based approach as fallback
	        return PDImageXObject.createFromFile(imageFile.getAbsolutePath(), document);
	    }
	}
	
	
	private void createPDFDocumentWithScreenshots(String pdfPath, File[] screenshotFiles) {
		// Combine all screenshots into a single PDF
		int successfullyAddedImages = 0;

		try(PDDocument document = new PDDocument();) {
			for (File imageFile : screenshotFiles) {
				try {
					if (imageFile.length() == 0) {
						log("Skipping empty image file: " + imageFile.getName());
						continue;
					}

					PDPage page = new PDPage(PDRectangle.A4);
					document.addPage(page);

					// Create image from file safely
					PDImageXObject image = null;
					try {
						image = createImageXObjectSafely(imageFile, document);

						// Verify image dimensions are valid
						if (image.getWidth() <= 0 || image.getHeight() <= 0) {
							logWarn("Invalid image dimensions in file: " + imageFile.getName()
									+ " - Width: " + image.getWidth() + ", Height: " + image.getHeight());
							continue;
						}
					} catch (Exception e) {
						logError("TestResultsAPI - Failed to process image: " + imageFile.getName() + ": "
								+ e.getMessage());
						continue; // Skip this image but try to continue with others
					}

					// Scale image to fit page
					float scale = calculateSafeScale(image.getWidth(), image.getHeight(),
							page.getMediaBox().getWidth(), page.getMediaBox().getHeight());

					float width = image.getWidth() * scale;
					float height = image.getHeight() * scale;
					float x = (page.getMediaBox().getWidth() - width) / 2;
					float y = (page.getMediaBox().getHeight() - height) / 2;

					PDPageContentStream contentStream = new PDPageContentStream(document, page);
					contentStream.drawImage(image, x, y, width, height);
					contentStream.close();
					successfullyAddedImages++;
				} catch (Exception e) {
					logError("TestResultsAPI - Error processing screenshot: " + imageFile.getName() + ": " + e.getMessage());
					// Continue with next image
				}
			}

			// If no images were successfully added, add a text page
			if (successfullyAddedImages == 0) {
				log("TestResultsAPI - No images could be processed, adding text-only page");
				addTextPage(document, "No screenshots could be processed.\nPlease check image files for corruption.");
			}

			// Save PDF file
			document.save(pdfPath);
			log("TestResultsAPI - Successfully created PDF with " + successfullyAddedImages
					+ " screenshots (out of " + screenshotFiles.length + "): " + pdfPath);
		} catch (Exception e) {
			logError("TestResultsAPI - Failed to create PDF from screenshots: " + e.getMessage());
			getStackTraceAsString(e);
		}
	}
}
