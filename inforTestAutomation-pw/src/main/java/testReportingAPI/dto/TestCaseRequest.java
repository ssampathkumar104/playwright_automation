package testReportingAPI.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Contains variables related to TestCase table
 */
@Data
public class TestCaseRequest {
	private Long suiteId;
	private String testCase;
	private OffsetDateTime testStartTime;
	private String runStatus;
}