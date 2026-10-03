package testReportingAPI;

import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Service  {

	private final String bucketName;
	private final String region;
	private final S3Client s3Client;

	/**
	 * API-specific logger method to redirect logs to file
	 * 
	 * @param message Message to log
	 */
	private void apiLog(String message) {
		APILoggerConfig.log(message);
	}

	public S3Service() {

		Properties prop = ConfigLoader.getEnvProperties();
		// Use the proper methods from ConfigLoader to get the AWS configuration
		this.bucketName = (String) prop.getOrDefault("AWS_STORAGE_BUCKET_NAME", "ipc-bucket"); 
		this.region = (String) prop.getOrDefault("AWS_S3_REGION_NAME", "us-east-1"); 


		String accessKey = (String) prop.getProperty("AWS_ACCESS_KEY_ID"); 
		String secretKey = (String) prop.getProperty("AWS_ACCESS_SECRET_KEY"); 

		apiLog("S3Service - Bucket name: " + bucketName);
		apiLog("S3Service - Region: " + region);
		apiLog("S3Service - Access key available: " + (accessKey != null && !accessKey.isEmpty()));

		if (StringUtils.isNoneBlank(accessKey, secretKey)) {
			this.s3Client = S3Client.builder().region(Region.of(region)).credentialsProvider(
					StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))).build();
			apiLog("S3Service - Initialized with provided credentials");
		} else {
			this.s3Client = S3Client.builder().region(Region.of(region))
					.credentialsProvider(DefaultCredentialsProvider.create()) // Use IAM Role on EC2
					.build();
			apiLog("S3Service - Initialized with default credentials provider");
		}
	}

	public String uploadFile(String keyName, String filePath) {
		try {
			// Extract file type from keyName (e.g., "DB/20240409/123/log" -> "log")
			String fileType = keyName.substring(keyName.lastIndexOf(".") + 1);

			// Get file extension based on file type
			String extension = "";
			switch (fileType) {
				case "log":
					extension = ".txt";
					break;
				case "artefact":
					extension = ".docx";
					break;
				case "screenshot":
					extension = ".pdf";
					break;
			}

			// Build key in Django format:
			// {type}s/{suiteId}/{caseId}/{scriptId}_{uniqueId}.{extension}
//			String key = String.format("%ss/%s/%s/%s_%s%s", fileType, suiteId, caseId, scriptId, uniqueId, extension);

			apiLog("S3Service - Attempting to upload file: " + filePath);
			apiLog("S3Service - Using S3 key: " + keyName);
			apiLog("S3Service - File type: " + fileType);

			// Add content type based on file extension
			PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder().bucket(bucketName).key(keyName);

			// Set content type based on file extension
			if (extension.equals(".txt")) {
				requestBuilder.contentType("text/plain");
			} else if (extension.equals(".docx")) {
				requestBuilder.contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			} else if (extension.equals(".pdf")) {
				requestBuilder.contentType("application/pdf");
			}

			PutObjectRequest putObjectRequest = requestBuilder.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));

			String s3Uri = String.format("s3://%s/%s", bucketName, keyName);
			apiLog("S3Service - Successfully uploaded file to S3 URI: " + s3Uri);
			return s3Uri;
		} catch (Exception e) {
			APILoggerConfig.logError("S3Service - Failed to upload file to S3: " + e.getMessage());
			APILoggerConfig.getStackTraceAsString(e);
			return null;
		}
	}
}
