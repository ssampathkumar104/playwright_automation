package testReportingAPI.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Contains variables related to updating TestCase table
 */
@Data
public class TestCaseUpdateRequest {
	private OffsetDateTime testEndTime;
	private String runStatus;
	private String failureReason;
}