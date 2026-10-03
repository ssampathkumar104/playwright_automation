//package testBase;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Collections;
//import java.util.Set;
//
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.FileEntity;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.client.methods.CloseableHttpResponse;
//
//import com.microsoft.aad.msal4j.ClientCredentialFactory;
//import com.microsoft.aad.msal4j.ClientCredentialParameters;
//import com.microsoft.aad.msal4j.ConfidentialClientApplication;
//import com.microsoft.aad.msal4j.IAuthenticationResult;
//import com.microsoft.aad.msal4j.IClientCredential;
//import com.microsoft.aad.msal4j.MsalException;
//import com.microsoft.aad.msal4j.SilentParameters;
//
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//
//public class SharepointActions {
//	
//	private static final String CONFIG_FOLDER = System.getProperty("user.dir") + File.separator + 
//			"src" + File.separator + "main" + File.separator + "java" + File.separator +
//			"config" + File.separator;
//	
//	private static String configFolder() {
//		return Files.exists(Paths.get(CONFIG_FOLDER)) ? CONFIG_FOLDER
//				: System.getProperty("user.dir") + File.separator  + "config" + File.separator;
//	}
//	
//	public static void updateSharepointJSONFile(String sharePointURL) {
//		if (!sharePointURL.isEmpty()) {
//			try {
//				JSONParser parser = new JSONParser();
//				Object obj = parser.parse(new FileReader(configFolder() + "sharepointDetails.json"));
//				JSONObject sharepointDetails = (JSONObject) obj;
//				
//				if (sharepointDetails.get("url").toString().isEmpty()) {
//					sharepointDetails.put("url", sharePointURL);
//					try (FileWriter fileWriter = new FileWriter(configFolder() + "sharepointDetails.json")) {
//						fileWriter.write(sharepointDetails.toJSONString());
//					}
//				}
//			} catch (Exception e) {
//				System.err.println("Please create 'sharepointDetails.json' at: " + configFolder());
//			}
//		}
//	}
//	
//	public static void uploadHTMLToSharePoint() {
//		try {
//			JSONParser parser = new JSONParser();
//			Object obj = parser.parse(new FileReader(configFolder() + "sharepointDetails.json"));
//			JSONObject sharepointDetails = (JSONObject) obj;
//			
//			if (sharepointDetails.get("url").toString().isEmpty()) return;
//			
//			Set<String> scope = Collections.singleton("https://inforonline.sharepoint.com/.default");
//			String accessToken = generateAccessToken(
//				sharepointDetails.get("client_id").toString(),
//				sharepointDetails.get("cert_password").toString(),
//				sharepointDetails.get("cert_path").toString(),
//				scope,
//				sharepointDetails.get("authority").toString()
//			);
//			
//			try (CloseableHttpClient client = HttpClients.createDefault()) {
//				String pathOfExtent = ThreadUtils.getExtentReportPath();
//				String nameOfHTMLFile = pathOfExtent.substring(pathOfExtent.lastIndexOf("\\") + 1);
//				String url = sharepointDetails.get("url").toString().replace("%s", nameOfHTMLFile);
//				
//				HttpPost request = new HttpPost(url);
//				request.addHeader("Authorization", "Bearer " + accessToken);
//				request.addHeader("Content-Type", "text/html");
//				request.setEntity(new FileEntity(new File(pathOfExtent), ContentType.create("text/html", "UTF-8")));
//				
//				try (CloseableHttpResponse response = client.execute(request)) {
//					System.out.println(response.getStatusLine().getStatusCode() == 200 
//						? "======= Successfully uploaded HTML to SharePoint ======="
//						: "======= Failed to upload HTML to SharePoint =======");
//				}
//			}
//			
//			sharepointDetails.put("url", "");
//			try (FileWriter fileWriter = new FileWriter(configFolder() + "sharepointDetails.json")) {
//				fileWriter.write(sharepointDetails.toJSONString());
//			}
//		} catch (Exception e) {
//			System.err.println("Please create 'sharepointDetails.json' at: " + configFolder());
//		}
//	}
//
//	public static String generateAccessToken(String client_id, String cert_password, String cert_path, Set<String> scope, String authority) throws Exception {
//		try (InputStream pkcs12Certificate = new FileInputStream(cert_path)) {
//			IClientCredential credential = ClientCredentialFactory.createFromCertificate(pkcs12Certificate, cert_password);
//			ConfidentialClientApplication cca = ConfidentialClientApplication
//				.builder(client_id, credential)
//				.authority(authority)
//				.build();
//			
//			IAuthenticationResult result;
//			try {
//				result = cca.acquireTokenSilently(SilentParameters.builder(scope).build()).join();
//			} catch (Exception ex) {
//				if (ex.getCause() instanceof MsalException) {
//					result = cca.acquireToken(ClientCredentialParameters.builder(scope).build()).join();
//				} else {
//					throw ex;
//				}
//			}
//			return result.accessToken();
//		}
//	}
//}