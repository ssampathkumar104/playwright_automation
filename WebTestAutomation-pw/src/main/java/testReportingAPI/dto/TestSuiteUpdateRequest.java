package testReportingAPI.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * Contains variables related to updating TestSuite table
 */
@Data
public class TestSuiteUpdateRequest {
	private String suiteStatus;
	private OffsetDateTime suiteEndDate;
}