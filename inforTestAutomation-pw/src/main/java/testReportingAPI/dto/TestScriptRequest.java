package testReportingAPI.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Contains variables related to TestScript table
 */
@Data
public class TestScriptRequest {
	private Long testCaseId;
	private String script;
	private Boolean isDependency;
	private OffsetDateTime scriptStartTime;
	private OffsetDateTime scriptEndTime;
	private String runStatus;
	private String failureReason;
	private String s3LogFile;
	private String s3Screenshots;
	private String s3Artifact;
}