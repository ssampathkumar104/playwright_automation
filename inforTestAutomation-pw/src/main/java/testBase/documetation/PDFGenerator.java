package testBase.documetation;

import static testBase.ThreadUtils.setPDFReportPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import testBase.BaseClass;
import testBase.ThreadUtils;


public class PDFGenerator {
	private PDFGenerator() {
	}
	
	public static final float FACTOR = 0.5f;
	
	public static void generatePDF(String methodName, String pdfReportPath, String startTime, String endTime, String status) {
	
		String path = pdfReportPath + ".pdf" ; 
		File failedCases = new File(pdfReportPath + ".pdf");

		ThreadUtils.getPDFReportObj().pdfReportFilePath = path;
		writeToPDF(methodName, failedCases, startTime, endTime, status);
	
		try {
			FileUtils.deleteDirectory(new File(pdfReportPath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void generatePDF(String testCaseName, String startTime, String endTime, String status)
			throws IOException {

		String pdfFileDir =ThreadUtils.getTempDirectoryPath() + testCaseName + File.separator + "artefact" + File.separator;
		String pdfFile = pdfFileDir + testCaseName + ".pdf";
		File fname = new File(pdfFileDir);
		if (!fname.exists())
			fname.mkdirs();

		new File(pdfFile).delete();
		new File(pdfFile).createNewFile();
		
		ThreadUtils.getPDFReportObj().pdfReportFilePath = pdfFile;
		setPDFReportPath(pdfFileDir);
		writeToPDF(testCaseName, new File(pdfFile), startTime, endTime, status);
	}
	
	public static void writeToPDF(String methodName, File f, String startTime, String endTime, String status) {
		Document pdfDocument = new Document(PageSize.A4, 36, 36, 90, 36);
		try {
			PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(f));
			PDFHeaderFooterPageEvent event = new PDFHeaderFooterPageEvent(methodName);
			writer.setPageEvent(event);
			pdfDocument.open();
			
			Paragraph p = new Paragraph("AUTOMATED TEST RESULTS SUMMARY", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(70, 130, 180)));
			p.setAlignment(Element.ALIGN_CENTER);
			pdfDocument.add(p);
			
			Paragraph ph = new Paragraph(new Paragraph(" "));
			PdfPCell cell = new PdfPCell(ph);
			cell.setBorder(Rectangle.BOTTOM);
			cell.setBorderColor(BaseColor.BLACK);
			cell.setBorderWidth(1f);
			
			PdfPTable myTable = new PdfPTable(1);
			myTable.addCell(cell);
			myTable.setHorizontalAlignment(Element.ALIGN_LEFT);
			myTable.setWidthPercentage(100f);
			pdfDocument.add(myTable);
			
			insertSpace(pdfDocument);
			
			String url = null;
			url = BaseClass.getParameter("BASE_URL");
			if (!StringUtils.isBlank(url)) {
				Paragraph p1 = new Paragraph("Environment URL : ", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD));
				p1.add(new Phrase(url, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(70, 130, 180))));
				pdfDocument.add(p1);
				insertSpace(pdfDocument);
			}
			
			Paragraph tableHeader = new Paragraph("EXECUTIVE SUMMARY - RESULTS", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(70, 130, 180)));
			tableHeader.setAlignment(Element.ALIGN_LEFT);
			pdfDocument.add(tableHeader);
			insertSpace(pdfDocument);

			Font headerCellFont = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.WHITE);
			Font normalCellFont = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(70, 130, 180));
			Font statusColor = null;
			if (status.equalsIgnoreCase("passed"))
				statusColor = new Font(FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(19, 131, 67));
			else if (status.equalsIgnoreCase("skipped"))
				statusColor = new Font(FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(192, 209, 35));
			else
				statusColor = new Font(FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.RED);

			Paragraph p3 = new Paragraph();
			// specify column widths
			float[] columnWidths = { 1f, 3.5f };
			// create PDF table with the given widths
			PdfPTable table = new PdfPTable(columnWidths);
			// set table width a percentage of the page width
			table.setWidthPercentage(100f);

			// insert column headings
			insertHeaderCell(table, " Test Name:", Element.ALIGN_LEFT, 1, headerCellFont);
			insertHeaderCell(table, " " + methodName, Element.ALIGN_LEFT, 1, headerCellFont);
			table.setHeaderRows(1);

			insertCell(table, " Execution Start Time:", Element.ALIGN_LEFT, 1, normalCellFont);
			insertCell(table, " " + startTime.replace("am", "AM").replace("pm", "PM") + "\n", Element.ALIGN_LEFT, 1, normalCellFont);

			insertCell(table, " Execution End Time:", Element.ALIGN_LEFT, 1, normalCellFont);
			insertCell(table, " " + endTime.replace("am", "AM").replace("pm", "PM") + "\n", Element.ALIGN_LEFT, 1, normalCellFont);

			insertCell(table, " Timezone Note: ", Element.ALIGN_LEFT, 1, normalCellFont);
			insertCell(table, " " + "Timestamps in screenshots are in " + genericCurrentDate("z") +" (" +timeZone() + ")" 
					+ " and are taken from local machine" + "\n", Element.ALIGN_LEFT, 1, normalCellFont);

			insertCell(table, " Execution Duration:", Element.ALIGN_LEFT, 1, normalCellFont);
			insertCell(table, " " + timeDuration(startTime, endTime) + " seconds \n", Element.ALIGN_LEFT, 1, normalCellFont);

			insertCell(table, " Execution Status:", Element.ALIGN_LEFT, 1, normalCellFont);
			insertCell(table, " " + status + "\n", Element.ALIGN_LEFT, 1, statusColor);

			p3.add(table);
			pdfDocument.add(p3);

			insertSpace(pdfDocument);

			String description = ThreadUtils.getPDFReportObj().getTestDescription();
			
			if (StringUtils.isNotBlank(description)) {
				insertSpace(pdfDocument);
				insertSpace(pdfDocument);
				Paragraph p4 = new Paragraph();
				float[] columnWidth = { 5f };
				PdfPTable table2 = new PdfPTable(columnWidth);
				table2.setWidthPercentage(100f);
				insertCell(table2, description, Element.ALIGN_LEFT, 1, normalCellFont);
				p4.add(table2);
				pdfDocument.add(p4);
			}

			pdfDocument.newPage();

			insertSpace(pdfDocument);
			Paragraph stepsHeader = new Paragraph("DETAILED DESCRIPTION OF THE TEST AND CORRESPONDING STEPS",
					new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(31, 73, 125)));
			stepsHeader.setAlignment(Element.ALIGN_LEFT);
			pdfDocument.add(stepsHeader);
			insertSpace(pdfDocument);
			insertSpace(pdfDocument);
			
			int i = 1 , sideHeader = 0;
			List<ArtefactObject> aoLst = ThreadUtils.getArtefactRef();
			for (ArtefactObject ao : aoLst) {
				if(!Objects.isNull(ao.getScreenImg())) {
						try {
							Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
							String desc =  ao.getDesc();
							
							if(ao.getDesc().equalsIgnoreCase("Test script failed point"))
								font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.RED);
							
							if (ao.getDesc().startsWith("_INFO : ")) {
								desc = desc.substring(1);
								insertSpace(pdfDocument);
								Paragraph impageText = new Paragraph(desc, font);
								impageText.setAlignment(Element.ALIGN_LEFT);
								pdfDocument.add(impageText);
								insertSpace(pdfDocument);
								insertSpace(pdfDocument);
							}else {
								Paragraph impageText = new Paragraph(desc, font);
								impageText.setAlignment(Element.ALIGN_LEFT);
								pdfDocument.add(impageText);
								insertSpace(pdfDocument);
								addImage(pdfDocument, ao.getScreenImgPath());
							}
							
							if (i % 2 == 0 && (aoLst.size() - sideHeader) != 1) {
								pdfDocument.newPage();
								pdfDocument.add(new Paragraph("\n"));
							}
							i++;
						} catch (Exception e) {
							e.printStackTrace();
						}
//					}	
				}else {
					sideHeader = sideHeader + 1;
				}
			}
			insertSpace(pdfDocument);
			insertSpace(pdfDocument);
			Paragraph endText = new Paragraph("END of TEST",
					new Font(Font.FontFamily.HELVETICA, 20, Font.BOLDITALIC, new BaseColor(128, 128, 128)));
			endText.setAlignment(Element.ALIGN_CENTER);
			pdfDocument.add(endText);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			pdfDocument.close();
		}
	}
	
	private static void addImage(Document document, String imagePath) throws DocumentException, IOException {
        // Create a table with 2 columns
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        // Create a cell for the image
        PdfPCell imageCell = new PdfPCell();
        imageCell.setBorder(Rectangle.NO_BORDER);
        Image image = Image.getInstance(imagePath);
        image.scaleToFit(525, 525); // Adjust image size as needed
        imageCell.addElement(image);
        table.addCell(imageCell);
        document.add(table);
    }

	private static void insertHeaderCell(PdfPTable table, String text, int align, int colspan, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(32f,text, font));
		cell.setBorderWidth(0.25f);
		cell.setBorderColor(BaseColor.BLACK);
		cell.setBackgroundColor(new BaseColor(73, 136, 208));
		cell.setHorizontalAlignment(align);
		cell.setExtraParagraphSpace(5);
		cell.setLeading(2f, 1f);
		cell.setIndent(3);
		cell.setFollowingIndent(3);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		// set the cell column span in case you want to merge two or more cells
		cell.setColspan(colspan);
		// in case there is no text and you wan to create an empty row
		if (text.trim().equalsIgnoreCase("")) {
			cell.setMinimumHeight(10f);
		}
		// add the call to the table
		table.addCell(cell);
	}

	private static void insertCell(PdfPTable table, String text, int align, int colspan, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBorderWidth(0.25f);
		cell.setBorderColor(BaseColor.BLACK);
		cell.setHorizontalAlignment(align);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setExtraParagraphSpace(3);
		cell.setIndent(3);
		cell.setFollowingIndent(3);
		// set the cell column span in case you want to merge two or more cells
		cell.setColspan(colspan);
		// in case there is no text and you wan to create an empty row
		if (text.trim().equalsIgnoreCase("")) {
			cell.setMinimumHeight(10f);
		}
		// add the call to the table
		table.addCell(cell);
	}

	private static long timeDuration(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
		Date firstDate = null;
		Date secondDate = null;
		try {
			firstDate = sdf.parse(startTime);
			secondDate = sdf.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
		return TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	private static void insertSpace(Document pdfDocument) {
		Paragraph tableHeaderSpace = new Paragraph();
		tableHeaderSpace.add("\n");
		tableHeaderSpace.setAlignment(Element.ALIGN_LEFT);
		try {
			pdfDocument.add(tableHeaderSpace);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	private static String timeZone() {
		DateTimeFormatter offsetFormatter = DateTimeFormatter.ofPattern("OOOO");
		TimeZone tz = TimeZone.getDefault();
		ZoneOffset offsetToday = OffsetDateTime.now(tz.toZoneId()).getOffset();
		return offsetFormatter.format(offsetToday);
	}
	
	private static String genericCurrentDate(String dateFormat) {
		SimpleDateFormat date = new SimpleDateFormat(dateFormat);
//		date.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
		Calendar cal = Calendar.getInstance();
		return date.format(cal.getTime()).replace("am", "AM").replace("pm", "PM");
	}

}
