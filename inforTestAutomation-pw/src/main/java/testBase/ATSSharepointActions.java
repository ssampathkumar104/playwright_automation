package testBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * SharePoint integration utility class for uploading PDF files using Microsoft Graph API.
 */
public class ATSSharepointActions extends BaseClass {
	
	private static final String GRAPH_API_BASE = "https://graph.microsoft.com/v1.0";
	private static final String siteHostname  = "inforonline.sharepoint.com";
	
	
	private static final String CONFIG_FOLDER = System.getProperty("user.dir") + File.separator + "src" + File.separator
			+ "main" + File.separator + "java" + File.separator + "config" + File.separator;

	private static String configFolder() {
		return Files.exists(Paths.get(CONFIG_FOLDER)) ? CONFIG_FOLDER
				: System.getProperty("user.dir") + File.separator + "config" + File.separator;
	}
	
	/**
	 * Uploads all PDF files from a folder to SharePoint.
	 * @param localFolder Local folder path containing PDF files
	 * @param siteHostname SharePoint site hostname
	 * @param sitePath SharePoint site path
	 * @param targetFolder Target folder in SharePoint
	 * @return true if all uploads successful, false otherwise
	 */
	public static boolean uploadPdfFolderToSharePoint(String localFolder, String customer, String runNumber) {
		try {
			Properties props = loadSharepointProperties();
			String propTenantId = props.getProperty("TENANT_ID");
			String propClientId = props.getProperty("CLIENT_ID");
			String propClientSecret = props.getProperty("CLIENT_SECRET");
			String propSitePath = props.getProperty("sitePath");
			String propTargetFolder = props.getProperty("targetFolder");
			
			String tokenUrl = "https://login.microsoftonline.com/" + propTenantId + "/oauth2/v2.0/token";
			String accessToken = getAccessToken(tokenUrl, propClientId, propClientSecret);
			if (accessToken == null) {
				System.err.println("Failed to obtain access token");
				return false;
			}
			
			String driveId = getDriveId(accessToken, siteHostname, propSitePath);
			if (driveId == null) {
				return false;
			}
			
			String uploadFolder = propTargetFolder + "/" + customer + "/" + runNumber;
			if (!createFolderPath(accessToken, driveId, uploadFolder)) {
				System.err.println("Failed to create folder: " + uploadFolder);
				return false;
			}
			
			List<String> pdfFiles = getPdfFiles(localFolder);
			if (pdfFiles.isEmpty()) {
				System.err.println("No PDF files found in folder: " + localFolder);
				return false;
			}
			
			boolean allSuccess = true;
			for (String fileName : pdfFiles) {
				String filePath = localFolder + File.separator + fileName;
				if (!uploadFileToFolder(accessToken, driveId, uploadFolder, fileName, filePath)) {
					allSuccess = false;
				}
			}
			
			return allSuccess;
		} catch (Exception e) {
			System.err.println("Failed to upload PDF folder to SharePoint: " + e.getMessage() + e);
			return false;
		}
	}
	
	/**
	 * Uploads a single file to SharePoint.
	 * @param testCaseName Name of the test case used to derive file path
	 * @return true if upload successful, false otherwise
	 */
	public static boolean uploadFileToSharePoint(String testCaseName) {
		if (!getParameter("Publish_TestResults", "no").equalsIgnoreCase("yes")) {
			return false;
		}
		try {
			String artifactPath = ThreadUtils.getTempDirectoryPath() + testCaseName + File.separator + "artefact" + File.separator;
			String generateDocument = getParameter("generateDocument", "false");
			String filePath = "";
			if (generateDocument.equalsIgnoreCase("docx"))
				filePath = artifactPath + testCaseName + ".docx";
			else if (generateDocument.equalsIgnoreCase("pdf"))
				filePath = artifactPath + testCaseName + ".pdf";
			
			String projectName = getParameter("PROJECT_NAME");
			String customer = StringUtils.isBlank(projectName) ? "default" : projectName;
			
			String runNumber = getParameter("RUN_NUMBER");
			if (StringUtils.isBlank(runNumber))
				Assert.fail("Run number need not be empty");
			
			File file = new File(filePath);
			if (!file.exists() || !file.isFile()) {
				System.err.println("File not found or is not a file: " + filePath);
				return false;
			}
			
			Properties props = loadSharepointProperties();
			String propTenantId = props.getProperty("TENANT_ID");
			String propClientId = props.getProperty("CLIENT_ID");
			String propClientSecret = props.getProperty("CLIENT_SECRET");
			String propSitePath = props.getProperty("sitePath");
			String propTargetFolder = props.getProperty("targetFolder");
			
			String tokenUrl = "https://login.microsoftonline.com/" + propTenantId + "/oauth2/v2.0/token";
			String accessToken = getAccessToken(tokenUrl, propClientId, propClientSecret);
			if (accessToken == null) {
				System.err.println("Failed to obtain access token");
				return false;
			}
			
			String driveId = getDriveId(accessToken, siteHostname, propSitePath);
			if (driveId == null) {
				return false;
			}
			
			String uploadFolder = propTargetFolder + "/" + customer + "/" + runNumber;
			if (!createFolderPath(accessToken, driveId, uploadFolder)) {
				System.err.println("Failed to create folder: " + uploadFolder);
				return false;
			}
			
			return uploadFileToFolder(accessToken, driveId, uploadFolder, file.getName(), filePath);
		} catch (Exception e) {
			System.err.println("Failed to upload file to SharePoint: " + e.getMessage() + e);
			return false;
		}
	}
	
	/**
	 * Loads SharePoint configuration properties from sharepointDetails.properties file.
	 */
	private static Properties loadSharepointProperties() throws IOException {
		Properties props = new Properties();
		String propsFile = configFolder() + "sharepointDetails.properties";
		try (FileInputStream fis = new FileInputStream(propsFile)) {
			props.load(fis);
		}
		return props;
	}
	
	/**
	 * Gets access token using client credentials flow with provided parameters.
	 */
	private static String getAccessToken(String tokenUrl, String clientId, String clientSecret) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost request = new HttpPost(tokenUrl);
			request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			
			String requestBody = "grant_type=client_credentials" +
					"&client_id=" + clientId +
					"&client_secret=" + clientSecret +
					"&scope=https://graph.microsoft.com/.default";
			
			request.setEntity(new StringEntity(requestBody));
			
			try (CloseableHttpResponse response = client.execute(request)) {
				String responseBody = EntityUtils.toString(response.getEntity());
				
				if (response.getStatusLine().getStatusCode() == 200) {
					JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
					return jsonResponse.get("access_token").getAsString();
				} else {
					System.err.println("Token request failed: " + responseBody);
					return null;
				}
			}
		}
	}
	
	/**
	 * Creates folder path in SharePoint drive, creating each level if it doesn't exist.
	 */
	private static boolean createFolderPath(String accessToken, String driveId, String folderPath) throws IOException {
		String[] parts = folderPath.split("/");
		String currentPath = "";
		
		for (String part : parts) {
			String parentPath = currentPath.isEmpty() ? "/root/children" : "/root:/" + currentPath + ":/children";
			String url = GRAPH_API_BASE + "/drives/" + driveId + parentPath;
			
			JsonObject body = new JsonObject();
			body.addProperty("name", part);
			body.add("folder", new JsonObject());
			body.addProperty("@microsoft.graph.conflictBehavior", "replace");
			
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				HttpPost request = new HttpPost(url);
				request.addHeader("Authorization", "Bearer " + accessToken);
				request.addHeader("Content-Type", "application/json");
				request.setEntity(new StringEntity(body.toString()));
				
				try (CloseableHttpResponse response = client.execute(request)) {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != 201 && statusCode != 200 && statusCode != 409) {
						System.err.println("Failed to create folder '" + part + "': " + EntityUtils.toString(response.getEntity()));
						return false;
					}
				}
			}
			currentPath = currentPath.isEmpty() ? part : currentPath + "/" + part;
		}
		return true;
	}
	
	/**
	 * Gets drive ID for a SharePoint site.
	 */
	private static String getDriveId(String accessToken, String siteHostname, String sitePath) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			// Get site info
			HttpGet siteRequest = new HttpGet(GRAPH_API_BASE + "/sites/" + siteHostname + ":/sites/" + sitePath);
			siteRequest.addHeader("Authorization", "Bearer " + accessToken);
			
			try (CloseableHttpResponse siteResponse = client.execute(siteRequest)) {
				if (siteResponse.getStatusLine().getStatusCode() != 200) {
					System.err.println("Failed to get site info");
					return null;
				}
				
				String siteResponseBody = EntityUtils.toString(siteResponse.getEntity());
				JsonObject siteJson = JsonParser.parseString(siteResponseBody).getAsJsonObject();
				String siteId = siteJson.get("id").getAsString();
				
				// Get drive info
				HttpGet driveRequest = new HttpGet(GRAPH_API_BASE + "/sites/" + siteId + "/drive");
				driveRequest.addHeader("Authorization", "Bearer " + accessToken);
				
				try (CloseableHttpResponse driveResponse = client.execute(driveRequest)) {
					if (driveResponse.getStatusLine().getStatusCode() != 200) {
						System.err.println("Failed to get drive info");
						return null;
					}
					
					String driveResponseBody = EntityUtils.toString(driveResponse.getEntity());
					JsonObject driveJson = JsonParser.parseString(driveResponseBody).getAsJsonObject();
					return driveJson.get("id").getAsString();
				}
			}
		}
	}
	
	/**
	 * Gets list of PDF files in a folder.
	 */
	private static List<String> getPdfFiles(String folderPath) {
		List<String> pdfFiles = new ArrayList<>();
		File folder = new File(folderPath);
		
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
			if (files != null) {
				for (File file : files) {
					pdfFiles.add(file.getName());
				}
			}
		}
		
		return pdfFiles;
	}
	
	/**
	 * Uploads file to specific SharePoint folder.
	 */
	private static boolean uploadFileToFolder(String accessToken, String driveId, String targetFolder, String fileName, String filePath) throws IOException {
		try {
			String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			String uploadUrl = GRAPH_API_BASE + "/drives/" + driveId + "/root:/" + targetFolder + "/" + encodedFileName + ":/content";
			return uploadFileContent(accessToken, uploadUrl, filePath);
		} catch (Exception e) {
			System.err.println("Failed to upload " + fileName + ": " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Uploads file content to SharePoint.
	 */
	private static boolean uploadFileContent(String accessToken, String uploadUrl, String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			System.err.println("File not found: " + filePath);
			return false;
		}
		
		byte[] fileContent = Files.readAllBytes(file.toPath());
		
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPut request = new HttpPut(uploadUrl);
			request.addHeader("Authorization", "Bearer " + accessToken);
			
			HttpEntity entity = new ByteArrayEntity(fileContent, ContentType.APPLICATION_OCTET_STREAM);
			request.setEntity(entity);
			
			try (CloseableHttpResponse response = client.execute(request)) {
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (statusCode == 200 || statusCode == 201) {
					BaseClass.log().info("Successfully uploaded PDF to SharePoint: " + file.getName());
					return true;
				} else {
					String responseBody = EntityUtils.toString(response.getEntity());
					System.err.println("Upload failed: " + responseBody);
					return false;
				}
			}
		}
	}
}