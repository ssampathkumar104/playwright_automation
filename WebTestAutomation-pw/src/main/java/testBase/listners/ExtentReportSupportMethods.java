package testBase.listners;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.testng.ITestResult;

import com.microsoft.playwright.Page;

import testBase.ArtefactBuilder;
import testBase.BaseClass;
import testBase.ThreadUtils;
import testReportingAPI.TestResultsAPI;

public class ExtentReportSupportMethods  extends TestResultsAPI {
	
	protected void handleFailure(ITestResult result, String colour) {
		String exceptionMsg = result.getThrowable().getMessage() != null ? result.getThrowable().getMessage() : "";
		String exception = Arrays.toString(result.getThrowable().getStackTrace());
		
		String failedImg="";
		try {
			failedImg = takeFailedScreenshot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ThreadUtils.setFailureSSBase64(failedImg);
		
		String imgSrcBase64 = "data:image/png;base64," + failedImg;
		String skipHtml = "<details><summary><b><font color = "+ colour +">Click to see, failure point of execution</font></b></summary>\n"
						+ "<div class=\"row mb-3\"><div style=\"text-align:center\" class=\"col-md-3\">"
						+ "<a href=\"" + imgSrcBase64 + "\" class=\"base64-img\" data-featherlight=\"image\">"
						+ "<img src=\"" + imgSrcBase64 + "\">"
						+ "</a></div></div></details> " ;
		
		String message = StringEscapeUtils.escapeHtml4(exceptionMsg).replaceAll("[\n]", "<br>")  + "<br> <br>";
		String exceptionLink = "<details><summary><b><font color = " + colour + ">"
								+ "Exception Occured, click to see details:"
								+ "</font></b></summary>"
								+ message + exception.replace(",", "<br>") 
								+ "</details> \n";
		
		if (result.getStatus() == ITestResult.FAILURE) {
			ThreadUtils.getExtent().fail(skipHtml);
			ThreadUtils.getExtent().fail(exceptionLink);
		}else if (result.getStatus() == ITestResult.SKIP) {
			ThreadUtils.getExtent().skip(skipHtml);
			ThreadUtils.getExtent().skip(exceptionLink);
		}
		
		ThreadUtils.getLogger().error("[ERROR] " + exceptionMsg.replaceAll("[\n]", "\n"));
		ThreadUtils.getLogger().error("[ERROR] " + exception.replaceAll("[\n]", "\n").replace(",", "\n"));
	}
	
	
	private String getLogBase64Content(String testCase, String fileName) {
		String logPath = ThreadUtils.getTempDirectoryPath() + testCase +  File.separator + "log" + File.separator + fileName ;
//		System.err.println(logPath);
		File logFile = new File(logPath);
		byte[] fileContent = null;

		try {
			fileContent = FileUtils.readFileToByteArray(logFile);
		}catch (FileNotFoundException e) {
			System.out.println("log file is not created");
		}catch (IOException e) {
			e.printStackTrace();
		}
		String fileEncoded = Base64.getEncoder().withoutPadding().encodeToString(fileContent);
		return "data:text/plain;charset=utf-8;base64," + fileEncoded;
	}
	
	private String getArtefactBase64Content(String testCaseName, String fName) {
		File file = null;
		String fPath = ThreadUtils.getTempDirectoryPath() + testCaseName +  File.separator + "artefact" + File.separator + fName;
		file = new File(fPath);
		
		byte[] fileContent = null;
		try {
			fileContent = FileUtils.readFileToByteArray(file);
		} catch (FileNotFoundException e) {
			System.out.println("Artefact file is not created : " + fPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fileEncoded = Base64.getEncoder().withoutPadding().encodeToString(fileContent);
		return "data:application/docx;base64," + fileEncoded;
	}
	
	private String takeFailedScreenshot() throws Exception {
		byte[] data = null;
		String image = null;
		if (ThreadUtils.getDriverRef() != null) {
			Page page = BaseClass.getDriver();
			if (page != null) {
				data = page.screenshot(new Page.ScreenshotOptions().setTimeout(30000));
				image = Base64.getEncoder().withoutPadding().encodeToString(data);
			}
		}
		else {
			try {
				Robot robot = new Robot();
				Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(screenFullImage, "png", baos);
				baos.flush();
				data = baos.toByteArray();
				image = Base64.getEncoder().withoutPadding().encodeToString(data);
			} catch (Exception e) {
				ThreadUtils.getLogger().error("[ERROR] Unable to capture the screenshot!", e);
			}
		}
		ArtefactBuilder.artefactSS("Test script failed point", null);
		return image;
	}
	
	protected String attachScreenShotsToReport(String testCaseName) throws NullPointerException, IOException {
		String htmlStart 	= "<details>"
								+ "<summary><b><font color = \"#04a1f4\">Click to see '" + testCaseName + "' screenshots</font></b></summary>\n"
								+ "<font color=\"black\">To see screenshot name, mouse over on the image <br>"	
								+ "To view a screenshot, right click on the image and open in new tab </font>"	
								+ "<div class=\"row mb-3\"><div style=\"text-align:center\" class=\"col-md-3\">";
		String htmlEnd 		= "</div></div></details>";
		String htmlMiddle 	= "<img  src =\"data:image/png;base64,%s\" "
								+ "title =\"%s\" "
								+ "style =\"border:2px solid gray; padding:2px; margin:2px\" "
//								+ " class=\"base64-img\" data-featherlight=\"image\">"
								+ ">"
								;
		
		StringBuilder sb = new StringBuilder();
		String sfPath = ThreadUtils.getTempDirectoryPath() + File.separator + testCaseName + File.separator + "screenshot" + File.separator;
		
		try {
			List<String> desc = FileUtils.readLines(new File(sfPath + "descriptionList.txt"));
			List<String> img  = ImageLister.getImageList(sfPath);
			
			for (int i=0; i < img.size(); i++) {
				String bs64 = Base64.getEncoder().withoutPadding().encodeToString(FileUtils.readFileToByteArray(new File(img.get(i))));
				sb.append(String.format(htmlMiddle, bs64, desc.get(i)));
			}
		
		
		}catch (FileNotFoundException e) {
			sb.append("");
			System.err.print("Screenshot are not available");
		}
		return htmlStart + sb + htmlEnd;
	}

	protected String attachArtefactFileToReport(String fileName) throws NullPointerException{
		String attachmentLink = "";
		if(BaseClass.getParameter("Generate_TestResultsDoc", "No").equalsIgnoreCase("yes") ||
				BaseClass.getParameter("generateDocument", "").equalsIgnoreCase("pdf") ||
				BaseClass.getParameter("generateDocument", "").equalsIgnoreCase("docx")||
				BaseClass.getParameter("generateDocument", "").equalsIgnoreCase("xls")) {
			String fDetailName =  fileName + "." + BaseClass.getParameter("generateDocument").toLowerCase();
			
			String fPath = System.getProperty("user.dir") + File.separator + "artefact" + File.separator + fDetailName;
			if (!new File(fPath).exists()) {
				fDetailName = ThreadUtils.getITestContext().getName().trim() + "."
						+ BaseClass.getParameter("generateDocument").toLowerCase();
			}
				
			StringBuilder attachmentLinkBuilder = new StringBuilder();
			attachmentLinkBuilder.append("<details><summary><b><font color = \"#04a1f4\" >Click to download '")
			                     .append(fileName)
			                     .append("' use Case document</font></b></summary>\n")
			                     .append("<div><div>")
			                     .append("<a href=\"")
			                     .append(getArtefactBase64Content(fileName, fDetailName))
			                     .append("\" download=\"")
			                     .append(fDetailName)
			                     .append("\" >")
			                     .append(fDetailName)
			                     .append("</a>")
			                     .append("</div></div></details>");

			attachmentLink = attachmentLinkBuilder.toString();
		}
		return attachmentLink;
	}
	
	protected String attachLogFilesToReport(String fileName) throws NullPointerException{
		String fDetailName = fileName + "_detailedLog.txt";
		String fActionName = fileName + "_actionLog.txt";
		
		StringBuilder attachmentLinkBuilder = new StringBuilder();

		attachmentLinkBuilder.append("<details><summary><b><font color = \"#04a1f4\" >Click to download '")
		                     .append(fileName)
		                     .append("' log files</font></b></summary>\n")
		                     .append("<div><div>")
		                     .append("<a href=\"")
		                     .append(getLogBase64Content(fileName, fDetailName))
		                     .append("\" download=\"")
		                     .append(fDetailName)
		                     .append("\" >")
		                     .append(fDetailName)
		                     .append("</a>")
		                     .append("<br>")
		                     .append("<a href=\"")
		                     .append(getLogBase64Content(fileName, fActionName))
		                     .append("\" download=\"")
		                     .append(fActionName)
		                     .append("\" >")
		                     .append(fActionName)
		                     .append("</a>")
		                     .append("</div></div></details>");

		return attachmentLinkBuilder.toString();

	}
}
