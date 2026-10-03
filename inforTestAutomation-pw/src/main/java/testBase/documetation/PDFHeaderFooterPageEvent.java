package testBase.documetation;

import java.io.IOException;
import java.net.MalformedURLException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import testBase.TestData;

/**
 * PDFHeaderFooterPageEvent is a class that represents a page event helper for adding headers and footers to a PDF document.
 * The PDFHeaderFooterPageEvent class extends the PdfPageEventHelper class to add header and footer to a PDF document.
 */

public class PDFHeaderFooterPageEvent extends PdfPageEventHelper {

	private PdfTemplate t;
	private Image total;
	private String testName;

	/**
     * Constructs a PDFHeaderFooterPageEvent object with the specified test name.
     *
     * @param testName the name of the test
     */
	PDFHeaderFooterPageEvent(String testName) {
		this.testName = testName;
	}

	 /**
     * Overrides the onOpenDocument method in PdfPageEventHelper to create a template for the total number of pages.
     *
     * @param writer   the PdfWriter instance
     * @param document the Document instance
     */
	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		t = writer.getDirectContent().createTemplate(30, 16);
		try {
			total = Image.getInstance(t);
			total.setRole(PdfName.ARTIFACT);
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	/**
     * Overrides the onEndPage method in PdfPageEventHelper to add the header and footer to each page.
     *
     * @param writer   the PdfWriter instance
     * @param document the Document instance
     */
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		addHeader(writer);
		addFooter(writer);
	}

	/**
     * Adds the header to the PDF document.
     *
     * @param writer the PdfWriter instance
     */
	private void addHeader(PdfWriter writer) {
		PdfPTable header = new PdfPTable(2);
		try {
			// set defaults
			header.setWidths(new int[] { 4, 24 });
			header.setTotalWidth(527);
			header.setLockedWidth(true);
			header.getDefaultCell().setFixedHeight(40);
			header.getDefaultCell().setBorder(Rectangle.BOX);
			header.getDefaultCell().setBorderColor(BaseColor.BLACK);

			// add image
//			String base64LogoImage = "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";
			String base64LogoImage = TestData.getCompanyLogo();
			byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64LogoImage);
			Image logo = Image.getInstance(imageBytes);
			header.addCell(logo);

			// add text
			PdfPCell text = new PdfPCell();
			text.setPaddingBottom(15);
			text.setPaddingLeft(10);
			text.setBorder(Rectangle.BOX);
			text.setBorderColor(BaseColor.BLACK);
			text.setVerticalAlignment(Element.ALIGN_MIDDLE);
			text.addElement(new Phrase(testName, new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.BLACK)));
//			text.addElement(new Phrase("<COMPANY_URL>", new Font(Font.FontFamily.HELVETICA, 8)));
			header.addCell(text);

			// write content
			header.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		} catch (MalformedURLException e) {
			throw new ExceptionConverter(e);
		} catch (IOException e) {
			throw new ExceptionConverter(e);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	*
	* Adds a footer to the PDF document.
	*
	* @param writer The PdfWriter object used to write the PDF document.
	*/
	private void addFooter(PdfWriter writer) {
		PdfPTable footer = new PdfPTable(3);
		try {
			// set defaults
			footer.setWidths(new int[] { 24, 24, 1 });
			footer.setTotalWidth(527);
			footer.setLockedWidth(true);
			footer.getDefaultCell().setFixedHeight(40);
			footer.getDefaultCell().setBorder(Rectangle.TOP);
			footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

			// Add copyright information
			footer.addCell(new Phrase("\u00A9 <COMPANY_NAME>", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));

			// add current page count
			footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			footer.addCell(new Phrase(String.format("Page %d of", writer.getPageNumber()),
					new Font(Font.FontFamily.HELVETICA, 8)));

			// add placeholder for total page count
			PdfPCell totalPageCount = new PdfPCell(total);
			totalPageCount.setBorder(Rectangle.TOP);
			totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
			footer.addCell(totalPageCount);

			// Write the footer on the page
			PdfContentByte canvas = writer.getDirectContent();
			canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
			footer.writeSelectedRows(0, -1, 34, 36, canvas);
			canvas.endMarkedContentSequence();
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	/**
	*
	* Overrides the onCloseDocument() method of the PdfPageEventHelper class.
	*
	* This method is called when the document is about to be closed.
	*
	* @param writer The PdfWriter object used to write the PDF document.
	*
	* @param document The Document object representing the PDF document.
	*/
	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {
		
		// Calculate the total length and width of the page number
		int totalLength = String.valueOf(writer.getPageNumber()).length();
		int totalWidth = totalLength * 5;
		
		// Show the page number aligned to the right on the document
		ColumnText.showTextAligned(t, Element.ALIGN_RIGHT,
				new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)), totalWidth, 6, 0);
	}
}
