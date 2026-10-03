package testBase.documetation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import testBase.TestData;
import testBase.ThreadUtils;

/**
 * Purpose: Generates DOCX and XLS test evidence documents with screenshots,
 * step tables, headers, footers, and summary information.
 */
public class DOCXGenerator {

	private DOCXGenerator() {
	}
	// ═══════════════════════════════════════════════════════════════════
	// PUBLIC API
	// ═══════════════════════════════════════════════════════════════════

	/**
	 * Purpose: Generates an Excel file with test case steps.
	 *
	 * @param testCaseName the name of the test case
	 * @param startTime    the start time of the test case
	 * @param endTime      the end time of the test case
	 * @param status       the status of the test case
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeToXLS(String testCaseName, String startTime, String endTime, String status)
			throws IOException {
		String excelFileDir = ThreadUtils.getTempDirectoryPath() + testCaseName + File.separator + "artefact"
				+ File.separator;
		String excelFilePath = excelFileDir + testCaseName + ".xls";
		ThreadUtils.getPDFReportObj().xlsReportFilePath = excelFilePath;
		List<ArtefactObject> aoLst = ThreadUtils.getArtefactRef();

		Files.createDirectories(Paths.get(excelFileDir));
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Test Case Steps");

		HSSFCellStyle boldStyle = createBoldStyle(workbook);
		createXlsHeader(sheet, boldStyle);
		populateXlsRows(sheet, aoLst, boldStyle);

		for (int i = 0; i < 3; i++)
			sheet.autoSizeColumn(i);
		try (FileOutputStream out = new FileOutputStream(excelFilePath)) {
			workbook.write(out);
		}
		workbook.close();
	}

	/**
	 * Purpose: Generates a DOCX file for a given test case with screenshots.
	 *
	 * @param testCaseName the name of the test case
	 * @param startTime    the start time of the test case
	 * @param endTime      the end time of the test case
	 * @param status       the status of the test case
	 * @throws IOException if an I/O error occurs
	 */
	public static void generateDOCX(String testCaseName, String startTime, String endTime, String status)
			throws IOException {
		String docxFileDir = ThreadUtils.getTempDirectoryPath() + testCaseName + File.separator + "artefact"
				+ File.separator;
		String docxFile = docxFileDir + testCaseName + ".docx";
		ThreadUtils.getPDFReportObj().docxReportFilePath = docxFile;

		File fname = new File(docxFile);
		if (!fname.exists())
			fname.mkdirs();
		new File(docxFile).delete();
		new File(docxFile).createNewFile();

		writeToDOCX(testCaseName, fname, startTime, endTime, status);
	}

	/**
	 * Purpose: Writes test case details (steps table + screenshots) to a DOCX file.
	 *
	 * @param methodName the name of the method
	 * @param f          the file to write to
	 * @param startTime  the start time of the test case
	 * @param endTime    the end time of the test case
	 * @param status     the status of the test case
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeToDOCX(String methodName, File f, String startTime, String endTime, String status)
			throws IOException {
		XWPFDocument document = null;
		FileOutputStream out = null;

		try {
			document = new XWPFDocument();
			out = new FileOutputStream(f, true);
			int stepCnt = 1;

			addHeaderFooter(document, methodName);
			addSummaryTable(document);

			List<ArtefactObject> aoLst = ThreadUtils.getArtefactRef();
			addTableForStepsHeader(document);
			XWPFTable stepsTable = addTableForSteps(document, aoLst);

			// Build steps table rows
			XWPFParagraph paragraph = document.createParagraph();
			XWPFRun run = paragraph.createRun();
			buildStepsTableRows(stepsTable, aoLst);

			// Page break + section title for screenshots
			run.addBreak(BreakType.PAGE);
			run.setText("Steps with Screenshots");
			run.setBold(true);
			run.setFontSize(12);
			run.addBreak();
			paragraph.setAlignment(ParagraphAlignment.CENTER);

			// Screenshot section
			XWPFParagraph paragraph2 = document.createParagraph();
			XWPFRun run2 = paragraph2.createRun();
			int sideHeader = 0;

			for (ArtefactObject aoRef : aoLst) {
				if (Objects.isNull(aoRef.getScreenImg())) {
					sideHeader++;
					continue;
				}

				FileInputStream fImg = new FileInputStream(aoRef.getScreenImg());
				if (aoRef.getDesc().equalsIgnoreCase("Test script failed point")) {
					// Failed step - red border screenshot
					XWPFRun run3 = paragraph2.createRun();
					run3.setText(aoRef.getDesc());
					run3.setBold(true);
					run3.setColor("FF0000");
					run3.addBreak(BreakType.TEXT_WRAPPING);
					XWPFPicture picture = run3.addPicture(fImg, Document.PICTURE_TYPE_PNG, f.getName(),
							Units.toEMU(450), Units.toEMU(250));
					addPictureBorder(picture, 2.25, new byte[] { (byte) 250, 0, 0 });

				} else if (aoRef.getDesc().startsWith("_INFO : ")) {
					// Info note - text only (no screenshot image)
					String noteText = aoRef.getDesc().replaceFirst("^_", "");
					if (noteText.contains("\n")) {
						String[] lines = noteText.split("\n");
						System.out.println(lines.length);
						run2.setText(String.valueOf(stepCnt) + ". " + lines[0].strip());
						for (int i = 1; i < lines.length; i++) {
							run2.addBreak(BreakType.TEXT_WRAPPING);
							if (!lines[i].strip().isEmpty()) {
								run2.setText(lines[i].strip());
							}
						}
					} else {
						run2.setText(String.valueOf(stepCnt) + ". " + noteText);
					}
					run2.setBold(false);
					run2.setColor("000000");

				} else {
					// Normal step - black border screenshot
					run2.setText(String.valueOf(stepCnt) + ". " + aoRef.getDesc());
					run2.setBold(false);
					run2.setColor("000000");
					run2.addBreak(BreakType.TEXT_WRAPPING);
					XWPFPicture picture = run2.addPicture(fImg, Document.PICTURE_TYPE_PNG, f.getName(),
							Units.toEMU(450), Units.toEMU(250));
					addPictureBorder(picture, 1, new byte[] { (byte) 0, 0, 0 });
				}

				// Page break after every 2 screenshots (except last step)
				if (stepCnt % 2 == 0 && (aoLst.size() - sideHeader) != stepCnt) {
					paragraph2 = document.createParagraph();
					paragraph2.setPageBreak(true);
					run2 = paragraph2.createRun();
				} else {
					run2.addBreak();
					run2.addBreak();
				}
				stepCnt++;
			}

			document.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
			if (document != null)
				document.close();
		}
	}
	// ═══════════════════════════════════════════════════════════════════
	// PRIVATE HELPERS - DOCUMENT STRUCTURE
	// ═══════════════════════════════════════════════════════════════════

	/**
	 * Purpose: Builds the steps table rows with step numbers, descriptions, and
	 * section headers.
	 */
	private static void buildStepsTableRows(XWPFTable stepsTable, List<ArtefactObject> aoLst) {
		for (int i = 0, j = 0; i < aoLst.size(); i++, j++) {
			XWPFTableRow r = stepsTable.getRow(i);
			if (!Objects.isNull(aoLst.get(i).getScreenImg())) {
				// Step Number
				XWPFTableCell c = r.getCell(0);
				setCellWidth(c, 800);
				c.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
				setCellTextWithCarriageReturn(c, String.valueOf(j + 1));
				setParaAndLineSpacing(c.getParagraphs().get(0), ParagraphAlignment.CENTER);

				// Step Description
				c = r.getCell(1);
				setCellWidth(c, 5600);
				c.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
				String text = aoLst.get(i).getDesc();
				setCellTextWithCarriageReturn(c, " " + (text.startsWith("_INFO : ") ? text.substring(1) : text));
				mergeCellsHorizontally(stepsTable, i, 1, 2);

				// Custom Data Column
				c = r.getCell(3);
				setCellWidth(c, 1600);
				setCellTextWithCarriageReturn(c, " ");
				setParaAndLineSpacing(c.getParagraphs().get(0), ParagraphAlignment.LEFT);
			} else {
				// Section header row (blue background, merged)
				XWPFTableCell c = r.getCell(0);
				setCellWidth(c, 8000);
				c.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
				setCellTextWithCarriageReturn(c, " " + aoLst.get(i).getDesc());
				setParaAndLineSpacing(c.getParagraphs().get(0), ParagraphAlignment.LEFT);
				c.getCTTc().addNewTcPr().addNewShd().setFill("cfe2f3");
				c.getParagraphs().get(0).getRuns().get(0).setBold(true);
				mergeCellsHorizontally(stepsTable, i, 0, 3);
				j--;
			}
		}
	}

	/**
	 * Purpose: Adds header and footer to the document.
	 */
	private static void addHeaderFooter(XWPFDocument document, String testcaseName)
			throws InvalidFormatException, IOException {
		XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
		if (headerFooterPolicy == null)
			headerFooterPolicy = document.createHeaderFooterPolicy();
		addHeader(headerFooterPolicy, testcaseName);
		addFooter(headerFooterPolicy);
	}

	/**
	 * Purpose: Adds header with logo and test case name.
	 */
	private static void addHeader(XWPFHeaderFooterPolicy headerFooterPolicy, String testcaseName)
			throws InvalidFormatException, IOException {
		byte[] imageBytes = DatatypeConverter.parseBase64Binary(TestData.getCompanyLogo());
		InputStream fImg = new ByteArrayInputStream(imageBytes);

		XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
		XWPFTable headerTable = header.createTable(1, 2);
		headerTable.setWidth("100%");
		XWPFTableRow headerRow = headerTable.getRow(0);

		// Logo cell
		XWPFRun logoRun = headerRow.getCell(0).getParagraphs().get(0).createRun();
		logoRun.addPicture(fImg, Document.PICTURE_TYPE_BMP, "Infor Logo", Units.toEMU(30), Units.toEMU(30));
		setParaAndLineSpacing(headerRow.getCell(0).getParagraphs().get(0), ParagraphAlignment.CENTER);
		headerRow.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

		// Test case name cell
		XWPFRun tcId = headerRow.getCell(1).getParagraphs().get(0).createRun();
		tcId.setTextPosition(2);
		tcId.setText(wrapLetters("\n\r \t " + testcaseName));
		tcId.setBold(true);
		tcId.setItalic(true);
		setParaAndLineSpacing(headerRow.getCell(1).getParagraphs().get(0), ParagraphAlignment.LEFT);
		headerRow.getCell(1).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
	}

	/**
	 * Purpose: Adds footer with copyright, date reference, and page numbers.
	 */
	private static void addFooter(XWPFHeaderFooterPolicy headerFooterPolicy) {
		XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
		XWPFTable footerTable = footer.createTable(1, 3);
		footerTable.setLeftBorder(XWPFBorderType.NONE, 0, 0, "0000");
		footerTable.setRightBorder(XWPFBorderType.NONE, 0, 0, "0000");
		footerTable.setInsideVBorder(XWPFBorderType.NONE, 0, 0, "0000");
		footerTable.setBottomBorder(XWPFBorderType.NONE, 0, 0, "0000");
		footerTable.setWidth("100%");

		// Copyright
		footerTable.getRow(0).getCell(0).setText("\u00A9 <COMPANY_NAME>");

		// Date reference
		XWPFParagraph datePara = footerTable.getRow(0).getCell(1).getParagraphs().get(0);
		datePara.createRun().setText("Doc Ref: " + DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(LocalDate.now()));
		datePara.setAlignment(ParagraphAlignment.CENTER);
		datePara.setBorderTop(Borders.NONE);

		// Page numbers
		XWPFParagraph pagePara = footerTable.getRow(0).getCell(2).getParagraphs().get(0);
		pagePara.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");
		pagePara.createRun().setText(" of ");
		pagePara.getCTP().addNewFldSimple().setInstr("NUMPAGES \\* MERGEFORMAT");
		pagePara.setAlignment(ParagraphAlignment.RIGHT);
		pagePara.setBorderTop(Borders.NONE);

		// Column widths
		setCellWidth(footerTable.getRow(0).getCell(0), 2000);
		setCellWidth(footerTable.getRow(0).getCell(1), 8000);
		setCellWidth(footerTable.getRow(0).getCell(2), 2000);
	}

	/**
	 * Purpose: Adds the summary table with general info, description,
	 * prerequisites, and notes.
	 */
	private static void addSummaryTable(XWPFDocument document) {
		document.createParagraph();

		String process = blankSafe(ThreadUtils.getPDFReportObj().getProcess());
		String usecaseId = blankSafe(ThreadUtils.getPDFReportObj().getUsecaseId());
		String user = blankSafe(ThreadUtils.getPDFReportObj().getUser());
		String prerequisites = blankSafe(ThreadUtils.getPDFReportObj().getPrerequisites());
		String notes = blankSafe(ThreadUtils.getPDFReportObj().getNotes());
		String description = blankSafe(ThreadUtils.getPDFReportObj().getDescription());

		// General Information row
		XWPFTable table1 = document.createTable(1, 4);
		table1.setWidth("100%");
		setTableBorders(table1, 16);

		XWPFTableRow row1 = table1.getRow(0);
		setCellWidth(row1.getCell(0), 1400);
		setCellWidth(row1.getCell(1), 2200);
		setCellWidth(row1.getCell(2), 2200);
		setCellWidth(row1.getCell(3), 2200);

		setHeaderCellTextWithCarriageReturn(row1.getCell(0), "General Information", "");
		row1.getCell(0).getCTTc().addNewTcPr().addNewShd().setFill("4988D0");
		row1.getCell(0).getParagraphs().get(0).getRuns().get(0).setBold(true);
		row1.getCell(0).getParagraphs().get(0).getRuns().get(0).setFontSize(12);
		setHeaderCellTextWithCarriageReturn(row1.getCell(1), "Process:", process);
		setHeaderCellTextWithCarriageReturn(row1.getCell(2), "Use Case Id:", usecaseId);
		setHeaderCellTextWithCarriageReturn(row1.getCell(3), "User:", user);

		// Details table (description, prerequisites, notes)
		XWPFTable table2 = document.createTable(3, 4);
		table2.setWidth("100%");
		table2.setBottomBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
		table2.setInsideHBorder(XWPFBorderType.THICK, 8, 0, "2E8BC0");
		table2.setInsideVBorder(XWPFBorderType.THICK, 8, 0, "2E8BC0");
		table2.setRightBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
		table2.setLeftBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
		XWPFTableRow row2 = table2.getRow(0);
		setCellWidth(row2.getCell(0), 1400);
		setCellWidth(row2.getCell(1), 2200);
		setCellWidth(row2.getCell(2), 2200);
		setCellWidth(row2.getCell(3), 2200);

		row2.getCell(0).setText("Description");
		row2.getCell(0).getParagraphs().get(0).getRuns().get(0).setBold(true);
		setCellTextWithCarriageReturn(row2.getCell(1), description);

		XWPFTableRow row3 = table2.getRow(1);
		row3.getCell(0).setText("Prerequisites");
		row3.getCell(0).getParagraphs().get(0).getRuns().get(0).setBold(true);
		setCellTextWithCarriageReturn(row3.getCell(1), prerequisites);

		mergeCellsHorizontally(table2, 0, 1, 3);
		mergeCellsHorizontally(table2, 1, 1, 3);

		// Notes row
		XWPFTableRow row4 = table2.getRow(2);
		row4.getCell(0).setText("Notes");
		row4.getCell(0).getParagraphs().get(0).getRuns().get(0).setBold(true);

		XWPFRun noteRun = null;
		if (isNotBlank(notes)) {
			noteRun = setCellTextWithCarriageReturn(row4.getCell(1), notes);
			noteRun.addBreak();
			noteRun.addBreak();
		}
		noteRun = setCellTextWithCarriageReturn(row4.getCell(1), "");
		noteRun.setBold(true);
		noteRun.setItalic(true);
		noteRun.setText(
				"The data shown is based on the Golden Tenant; customers will need to input their own data accordingly.");
		mergeCellsHorizontally(table2, 2, 1, 3);
	}

	/**
	 * Purpose: Adds the steps table header row with "Steps" and "Customer Data
	 * Value" columns.
	 */
	private static void addTableForStepsHeader(XWPFDocument document) throws InvalidFormatException, IOException {
		document.createParagraph().createRun().addBreak();
		XWPFTable stepsTableHeader = document.createTable(1, 4);
		stepsTableHeader.setWidth("100%");

		XWPFTableRow headerRow = stepsTableHeader.getRow(0);
		headerRow.setHeight(600);

		// "Steps" header
		headerRow.getCell(0).setText("Steps");
		setParaAndLineSpacing(headerRow.getCell(0).getParagraphs().get(0), ParagraphAlignment.CENTER);
		headerRow.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
		headerRow.getCell(0).getParagraphs().get(0).getRuns().get(0).setBold(true);
		headerRow.getCell(0).getParagraphs().get(0).getRuns().get(0).setFontSize(12);
		headerRow.getCell(0).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);

		// "Customer Data Value" header
		headerRow.getCell(3).setText("Customer Data Value");
		setParaAndLineSpacing(headerRow.getCell(3).getParagraphs().get(0), ParagraphAlignment.CENTER);
		headerRow.getCell(3).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
		headerRow.getCell(3).getParagraphs().get(0).getRuns().get(0).setBold(true);
		headerRow.getCell(3).getParagraphs().get(0).getRuns().get(0).setFontSize(12);
		headerRow.getCell(3).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);

		// Column widths
		setCellWidth(headerRow.getCell(0), 650);
		setCellWidth(headerRow.getCell(1), 2800);
		setCellWidth(headerRow.getCell(2), 2800);
		setCellWidth(headerRow.getCell(3), 1800);

		mergeCellsHorizontally(stepsTableHeader, 0, 0, 2);
		setTableBorders(stepsTableHeader, 16);

		// Background colors
		headerRow.getCell(0).getCTTc().addNewTcPr().addNewShd().setFill("ccddff");
		headerRow.getCell(3).getCTTc().addNewTcPr().addNewShd().setFill("ccddff");
	}

	/**
	 * Purpose: Creates the steps data table with the correct number of rows and
	 * column widths.
	 */
	private static XWPFTable addTableForSteps(XWPFDocument document, List<ArtefactObject> aoLst) {
		XWPFTable stepsTable = document.createTable(aoLst.size(), 4);
		if (aoLst.size() != 0) {
			stepsTable.setWidth("100%");
			stepsTable.setBottomBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
			stepsTable.setLeftBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
			stepsTable.setRightBorder(XWPFBorderType.THICK, 16, 0, "2E8BC0");
			stepsTable.setInsideHBorder(XWPFBorderType.THICK, 8, 0, "2E8BC0");
			stepsTable.setInsideVBorder(XWPFBorderType.THICK, 8, 0, "2E8BC0");

			setCellWidth(stepsTable.getRow(0).getCell(0), 600);
			setCellWidth(stepsTable.getRow(0).getCell(1), 2800);
			setCellWidth(stepsTable.getRow(0).getCell(2), 2800);
			setCellWidth(stepsTable.getRow(0).getCell(3), 1800);
		}
		return stepsTable;
	}

	// ═══════════════════════════════════════════════════════════════════
	// PRIVATE HELPERS - UTILITIES
	// ═══════════════════════════════════════════════════════════════════

	/**
	 * Purpose: Sets cell width using BigInteger.
	 */
	private static void setCellWidth(XWPFTableCell cell, int width) {
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(width));
	}

	/**
	 * Purpose: Adds a border to a picture element.
	 */
	private static void addPictureBorder(XWPFPicture picture, double width, byte[] color) {
		picture.getCTPicture().getSpPr().addNewLn().setW(Units.toEMU(width));
		picture.getCTPicture().getSpPr().getLn().addNewSolidFill().addNewSrgbClr().setVal(color);
	}

	/**
	 * Purpose: Sets standard blue borders on a table.
	 */
	private static void setTableBorders(XWPFTable table, int size) {
		table.setBottomBorder(XWPFBorderType.THICK, size, 16, "2E8BC0");
		table.setTopBorder(XWPFBorderType.THICK, size, 0, "2E8BC0");
		table.setInsideHBorder(XWPFBorderType.THICK, size, 0, "2E8BC0");
		table.setInsideVBorder(XWPFBorderType.THICK, 8, 0, "2E8BC0");
		table.setRightBorder(XWPFBorderType.THICK, size, 0, "2E8BC0");
		table.setLeftBorder(XWPFBorderType.THICK, size, 0, "2E8BC0");
	}

	/**
	 * Purpose: Returns trimmed string or empty if blank.
	 */
	private static String blankSafe(String value) {
		return isNotBlank(value) ? value.trim() : "";
	}

	/**
	 * Purpose: Wraps letters with zero-width spaces for word-breaking.
	 */
	private static String wrapLetters(String text) {
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			sb.append(c).append("\u200B");
		}
		return sb.toString();
	}

	/**
	 * Purpose: Sets paragraph alignment and line spacing properties.
	 */
	private static void setParaAndLineSpacing(XWPFParagraph para, ParagraphAlignment alignment) {
		para.setSpacingAfter(0);
		para.setSpacingBefore(0);
		para.setSpacingAfterLines(0);
		para.setSpacingBeforeLines(0);
		para.setWordWrapped(true);
		para.setSpacingBetween(0.9);
		para.setAlignment(alignment);
	}

	/**
	 * Purpose: Sets header cell text with a header label and value below it.
	 */
	private static void setHeaderCellTextWithCarriageReturn(XWPFTableCell cell, String header, String text) {
		XWPFRun run = cell.getParagraphs().get(0).createRun();
		setParaAndLineSpacing(cell.getParagraphs().get(0), ParagraphAlignment.LEFT);
		run.setText(header);
		if (!header.equalsIgnoreCase("General Information"))
			run.addBreak();
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			run.setText(lines[i]);
			if (i != lines.length - 1)
				run.addBreak();
		}
	}

	/**
	 * Purpose: Sets cell text with line breaks for multi-line content.
	 */
	private static XWPFRun setCellTextWithCarriageReturn(XWPFTableCell cell, String text) {
		XWPFRun run = cell.getParagraphs().get(0).createRun();
		setParaAndLineSpacing(cell.getParagraphs().get(0), ParagraphAlignment.LEFT);
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			run.setText(lines[i].strip());
			if (i != lines.length - 1)
				run.addBreak();
		}
		return run;
	}

	/**
	 * Purpose: Merges cells horizontally in a table row.
	 */
	private static void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
		for (int col = fromCol; col <= toCol; col++) {
			XWPFTableCell cell = table.getRow(row).getCell(col);
			cell.getCTTc().addNewTcPr().addNewHMerge().setVal(col == fromCol ? STMerge.RESTART : STMerge.CONTINUE);
		}
	}

	// ═══════════════════════════════════════════════════════════════════
	// PRIVATE HELPERS - XLS
	// ═══════════════════════════════════════════════════════════════════

	private static HSSFCellStyle createBoldStyle(HSSFWorkbook workbook) {
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.BOTTOM);
		return style;
	}

	private static void createXlsHeader(HSSFSheet sheet, HSSFCellStyle boldStyle) {
		HSSFRow headerRow = sheet.createRow(0);
		String[] headers = { "Step No.", "Step Description", "Customer Data" };
		for (int i = 0; i < headers.length; i++) {
			HSSFCell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(boldStyle);
		}
	}

	private static void populateXlsRows(HSSFSheet sheet, List<ArtefactObject> aoLst, HSSFCellStyle boldStyle) {
		int rowIndex = 1, stepNumber = 1;
		for (int i = 0; i < aoLst.size(); i++) {
			String desc = aoLst.get(i).getDesc();
			HSSFRow row = sheet.createRow(rowIndex);
			if (Objects.isNull(aoLst.get(i).getScreenImg())) {
				HSSFCell cell = row.createCell(0);
				cell.setCellValue(desc);
				cell.setCellStyle(boldStyle);
				sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 2));
			} else {
				row.createCell(0).setCellValue(stepNumber++);
				row.createCell(1).setCellValue(desc.startsWith("_INFO : ") ? desc.substring(1) : desc);
			}
			rowIndex++;
		}
	}
}
