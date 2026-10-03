package testBase.documetation;

import org.testng.ITestResult;

import lombok.Data;

/**
* This class represents a PDF Report object used for generating test reports in PDF format.
* It contains various properties that hold information related to the test execution and report generation.
* The properties include:
* testDescription: A string representing the description of the test.
* startTime: A string representing the start time of the test execution.
* endTime: A string representing the end time of the test execution.
* pdfReportFilePath: A string representing the file path where the PDF report will be saved.
* docxWoSSReportFilePath: A string representing the file path where the DOCX report without screenshots will be saved.
* result: An ITestResult object containing information about the test result.
* process: A string representing the process related to the test.
* usecaseId: A string representing the ID of the script/usecase related to the test.
* user: A string representing the user & roles associated with the test.
* prerequisites: A string representing the prerequisites for the test.
* Notes: A string representing the notes of the test.
*/
@Data
public class PDFReportObject {
	public String description;
	public String testDescription;
	public String startTime;
	public String endTime;
	public String pdfReportFilePath;
	public String docxReportFilePath;
	public String xlsReportFilePath;
	public ITestResult result;
	
	public String process;
	public String usecaseId;
	public String user;
	public String prerequisites;
	public String notes;
	
}