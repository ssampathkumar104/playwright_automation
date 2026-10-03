package testBase.listners;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.testng.ITestResult;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import testBase.BaseClass;
import testBase.ThreadUtils;
import testBase.documetation.DateFormat;

public class API_Call extends BaseClass {

	private static final String BASE_URL = "http://apso1wats4:8080/spring";
	private static String[] failureSnapshotPaths = null;
	public static String projectName;
	public static String runNumber;

	private static void getTCDetails(String testPlanName, String testCaseName, String startDate, String endDate,
			String status, String duration, String exception) throws IOException {
		
		// Artifact path
		String artifactPath = ThreadUtils.getTempDirectoryPath() + testCaseName + File.separator + "artefact" + File.separator;
		
		String fPath = "";
		if (getParameter("generateDocument", "false").equalsIgnoreCase("docx"))
			fPath = artifactPath + testCaseName + ".docx";
		if (getParameter("generateDocument", "false").equalsIgnoreCase("pdf"))
			fPath = artifactPath + testCaseName + ".pdf";
		
		// Log file path
		String logFilePath = ThreadUtils.getTempDirectoryPath() + testCaseName +  File.separator + "log" + File.separator + testCaseName + "_detailedLog.txt";

		// Failed snapshot path
//		String failedImagePath = null;
//		File file2 = new File(BaseClass.getPDFReportPath());
//		File[] files2 = file2.listFiles();
//		for (int i = 1; i < files2.length; i++) {
//			if (files2[i - 1].getName().contains("TestscriptFailedPoint.png")) {
//				failedImagePath = System.getProperty("user.dir") + File.separator + files2[i - 1].getPath();
//				System.out.println(failedImagePath);
//			}
//		}
		
		String failedImagePath = ThreadUtils.getArtefactDirectoryPath() + File.separator + "TestscriptFailedPoint.png";
		System.out.println("failedImagePath : " + failedImagePath);
//		if(!new File(failedImagePath).exists())
//			failedImagePath = null;
		
		// Retrieving Project name
		if (StringUtils.isBlank(getParameter("PROJECT_NAME")))
			projectName = "default";
		else
			projectName = getParameter("PROJECT_NAME");

		// Retrieving Run number
		if (StringUtils.isBlank(getParameter("RUN_NUMBER")))
			Assert.fail("Run number need not be empty");
		String runNumber = getParameter("RUN_NUMBER");
		String reRun = "No";

		// Retrieving hostname
		String hostname = "Unknown";
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException ex) {
			log().info("Hostname can not be resolved");
		}
		String executedBy = hostname;
		String startDate1 = (DateTimeUtils.dateFormatChange(startDate,DateFormat.dateFormatAPI).toString());// 04.02.2024 3:21:50PM
		String endDate1 = (DateTimeUtils.dateFormatChange(endDate,DateFormat.dateFormatAPI).toString());// 2024-11-05 02:30:08.000 
		String totalTimeTaken = duration;
		String buildNumber = "0";
		String issueType = "";

		String issueDescription = exception;// "remark1";
		String incidentReference = "";
		String incidentPlatForm = "";
		String reRunCount = "0";
		log().info(exception);

		String[] pdfFilePaths = { fPath };

		String[] textFilePaths = { logFilePath };

		FormDataMultiPart multiPart = new FormDataMultiPart();
		
		if (!StringUtils.isAllBlank(pdfFilePaths)) {
			for (String path : pdfFilePaths) {
				multiPart.bodyPart(
						new FileDataBodyPart("pdfFilesArray", new File(path), MediaType.APPLICATION_OCTET_STREAM_TYPE));
			}
		}

		for (String path : textFilePaths) {
			multiPart.bodyPart(
					new FileDataBodyPart("textFilesArray", new File(path), MediaType.APPLICATION_OCTET_STREAM_TYPE));
		}

		if (!status.equalsIgnoreCase("Pass")) {
			failureImage(failedImagePath);
			failureSnapshotPaths = new String[] { failedImagePath };
			for (String path : failureSnapshotPaths) {
				multiPart.bodyPart(new FileDataBodyPart("failureSnapshotArray", new File(path),
						MediaType.APPLICATION_OCTET_STREAM_TYPE));
			}
		} else {
			failureSnapshotPaths = null;
		}

		multiPart.field("projectName", projectName);
		multiPart.field("runNumber", runNumber);
		multiPart.field("testPlanName", testPlanName);
		multiPart.field("testCaseName", testCaseName);
		multiPart.field("status", status.substring(0, 4));
		multiPart.field("reRun", reRun);
		multiPart.field("executedBy", executedBy);
		multiPart.field("startDate", startDate1);
		multiPart.field("endDate", endDate1);
		multiPart.field("totalTimeTaken", totalTimeTaken);
		multiPart.field("buildNumber", buildNumber);
		multiPart.field("issueType", issueType);
		multiPart.field("issueDescription", issueDescription);
		multiPart.field("incidentReference", incidentReference);
		multiPart.field("incidentPlatform", incidentPlatForm);
		multiPart.field("reRunCount", reRunCount);

		Client client = ClientBuilder.newClient();

		Response response = client.target(BASE_URL).path("/TestexportReports")
				.request(MediaType.MULTIPART_FORM_DATA_TYPE)
				.post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			log().info("====== Response: " + response.getStatus());
			log().info("====== Test results published to the ATS database. ======");
		} else {
			log().error("Failed to upload file. Status code: " + response.getStatus());
			log().error("====== Unable to publish test results to the ATS database. ======");
		}

		response.close();
		client.close();
	}

	public static void publishToApi (ITestResult result, String testCaseName, String startTime, String endTime) {
		
		
		if (getParameter("Publish_TestResults","no").equalsIgnoreCase("yes")) {
			try {
				String strResult = result.getStatus()== ITestResult.SUCCESS ? "Pass" : (result.getStatus() == ITestResult.FAILURE ? "Fail" : "Skip");
				String suiteName = result.getTestContext().getSuite().getName();
				
			//	String exceptionMsg = result.getThrowable().getMessage() != null ? result.getThrowable().getMessage() : "";
				String exception = "";
				try {
					exception = Arrays.toString(result.getThrowable().getStackTrace());
				} catch(Exception e) {
					
				}
				getTCDetails(suiteName, testCaseName, startTime, endTime, strResult,
						DateTimeUtils.getDuration(startTime, endTime), exception);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void failureImage(String failedImagePath) {
		
        // Decode the base64 string
        byte[] imageBytes = Base64.getDecoder().decode(ThreadUtils.getFailureSSBase64());

        // Convert the byte array to a BufferedImage
        BufferedImage image = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            image = ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        // Save the BufferedImage to a file
        if (image != null) {
            try {
            	Files.createDirectories(Paths.get(failedImagePath).getParent());
                ImageIO.write(image, "png", new File(failedImagePath));
                System.out.println("Failre Image saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to convert failure base64 string to image.");
        }
    }
}
