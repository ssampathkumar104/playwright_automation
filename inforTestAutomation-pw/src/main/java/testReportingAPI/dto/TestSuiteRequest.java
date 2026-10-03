package testReportingAPI.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Contains variables related to TestScript table
 */
@Data
public class TestSuiteRequest {
	private String groupName;
	private String project;
	private String suite;
	private String suiteStatus;
	private String environment;
	private String releaseVersion;
	private OffsetDateTime suiteStartDate;
}