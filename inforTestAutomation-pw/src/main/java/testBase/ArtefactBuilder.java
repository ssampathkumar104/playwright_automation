package testBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import dataUtils.RuntimeData;
import testBase.documetation.ArtefactObject;
import testBase.documetation.DOCXGenerator;
import testBase.documetation.PDFGenerator;
import testBase.documetation.PDFReportObject;

/**
 *
 * ArtefactBuilder class defining ArtefactBuiler activities.
 */

public class ArtefactBuilder {

	private static String color = "";
	private static final String HIGHLIGHT_JS_PATH = "playwright/highlight4.js"; // work with js4 later
	private static final String DRAW_JS_PATH = "playwright/highlightDraw.js";
	
	
	private static final String HIGHLIGHT_SCRIPT = loadScript(HIGHLIGHT_JS_PATH);
	private static final String DRAW_SCRIPT = loadScript(DRAW_JS_PATH);

	private static String loadScript(String path) {
	    try (InputStream is = ArtefactBuilder.class.getClassLoader().getResourceAsStream(path)) {
	        if (is == null) {
	            throw new IllegalStateException("Cannot find script on classpath: " + path);
	        }
	        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
	    } catch (IOException e) {
	        throw new RuntimeException("Failed to load JS: " + path, e);
	    }
	}
	
	private static String getDrawScript() {
	    return DRAW_SCRIPT;
	}

	private static String getHighlightScript() {
		return HIGHLIGHT_SCRIPT;
	}

	private static String getColor() {
		List<String> list = Arrays.asList(color.split(","));
		return list.get(new Random().nextInt(list.size()));
	}
	
	private static void setColor(String colour) {
		color = colour;
	}
	
	/**
	 * Sets the custom action flag in ThreadUtils.
	 * always make sure that this is used in custom functions appropriately, code block should be between the false and true flags as below
	 * @param flag the value to set the custom action flag
	 */
	public static void setCustAct(Boolean flag) {
		ThreadUtils.setCustFlagRef(flag);
	}

	// constant
	private static final String GENERATE_DOCUMENT = "generateDocument";

	/**
	 * Takes an artifact screenshot with a description and a web element.
	 *
	 * @param description the description of the artifact
	 * @param e           the web element to capture in the screenshot
	 */
	protected static void takeArtefact(String description, Locator locator) {
		boolean flag = BaseClass.getParameter(GENERATE_DOCUMENT, "").trim().toLowerCase().matches("pdf|docx");
		if (!flag || !ThreadUtils.getCustFlagRef()) return;
		artefactSS(description, locator);
	}

	/**
	 * @param description text that needs to be add as sub header in the document
	 */
	public static void addSubHeader(String description) {
		boolean flag = BaseClass.getParameter(GENERATE_DOCUMENT, "").trim().toLowerCase().matches("pdf|docx|xls");
		if (!flag) {
			return;
		}

		File elementImg = null, screenImg = null;
		String screenImgPath = null;

		ArtefactObject ao = new ArtefactObject(description, elementImg, screenImg, screenImgPath);
		ThreadUtils.getArtefactRef().add(ao);
	}
	
	/**
	 * @param description text that needs to be add as sub header in the document
	 */
	public static void addInfoNotes(String description) {
		artefactSS("_INFO : " + description, null);
	}

	/**
	 * Takes artefact screenshots with descriptions and multiple web elements. Use
	 * this method only in custom functions
	 * 
	 * @param description the description of the artefacts
	 * @param ele         the web elements to capture in the screenshots
	 */
	public static void artefactSS(String description, Locator firstLocator, Locator... additionalLocators) {
	    boolean flag = BaseClass.getParameter(GENERATE_DOCUMENT, "").trim().toLowerCase().matches("pdf|docx|xls");
	    if (!flag) {
	        return;
	    }

	    File elementImg = null;

	    Locator[] locators = Stream
	            .concat(firstLocator != null ? Stream.of(firstLocator) : Stream.empty(), Stream.of(additionalLocators))
	            .toArray(Locator[]::new);

	    String color = getColor();
	    String fp = ThreadUtils.getArtefactDirectoryPath() + File.separator;
	    String name = RuntimeData.getRandomChars(11, 12) + RuntimeData.getDateParam("ddMMyyyyHHmmss");
	    String screenImgPath = fp + name.replaceAll("[^a-zA-Z0-9]", "") + ".png";
	    String elementImgPath = fp + "elements" + File.separator + RuntimeData.getDateParam("ddMMyyyyHHmmss") + "_.png";
	    new File(fp + "elements").mkdirs();

	    Page page = BaseClass.getDriver();

	    // Highlight elements
	    for (Locator locator : locators) {
	        if (locator != null) {
	            try {
	                if (locator.count() == 0 || !locator.first().isVisible()) {
	                    System.err.println("Highlight skipped: element not visible or virtualized");
	                    return;
	                }
	                try {
	                    locator.scrollIntoViewIfNeeded();
	                } catch (Exception e) {
	                    System.err.println("Failed to scroll element into view: ");
	                }

	                // Step 1: Run visibility checks inside the element's own frame
	                @SuppressWarnings("unchecked")
	                List<Map<String, Object>> debugInfo = (List<Map<String, Object>>) locator.evaluate(getHighlightScript(), color);

	                // Step 2: Get absolute coordinates via Playwright (handles any frame depth)
	                com.microsoft.playwright.options.BoundingBox box = locator.first().boundingBox();

	                // Step 3: Draw overlay on top-level page (works for frames, modals, everything)
	                if (box != null && debugInfo != null && !debugInfo.isEmpty()) {
	                    boolean shouldHighlight = debugInfo.stream().anyMatch(info ->
	                        Boolean.TRUE.equals(info.get("isInViewport")) &&
	                        Boolean.TRUE.equals(info.get("isTopElement")) &&
	                        Boolean.TRUE.equals(info.get("isCssVisible"))
	                    );
	                    if (shouldHighlight) {
	                        page.evaluate(getDrawScript(), Arrays.asList(box.x, box.y, box.width, box.height, color));
	                    }
	                }
	                
//					// Print debug info
//					for (Map<String, Object> info : debugInfo) {
//						System.err.println("%%%%%%%%%%%%%%%%%%%%%% START %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//						System.err.println("Element           : " + locator.toString());
//						System.err.println("Description       : " + description);
//						System.err.println("Is in viewport    : " + info.get("isInViewport"));
//						System.err.println("noOfVisiblePoints : " + info.get("noOfVisiblePoints"));
//						System.err.println("Is top element    : " + info.get("isTopElement"));
//						System.err.println("Is CSS Visible    : " + info.get("isCssVisible"));
//						System.err.println("Hit Element Tag   : " + info.get("hitElementTag"));
//						System.err.println("Hit Element Class : " + info.get("hitElementClass"));
//						System.err.println("Hit Element Id    : " + info.get("hitElementId"));
//						System.err.println("Rect              : " + info.get("rect"));
//						System.err.println("BoundingBox       : " + (box != null ? "x=" + box.x + " y=" + box.y + " w=" + box.width + " h=" + box.height : "null"));
//						System.err.println("%%%%%%%%%%%%%%%%%%%%%%% END %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//					}
	                
	            } catch (Exception e) {
	                System.err.println("Highlighting failed for an element: " + e.getMessage());
	            }
	        } else {
	            System.err.println("Locator is null, skipping highlight.");
	        }
	    }

	    byte[] screenImg = page.screenshot(new Page.ScreenshotOptions().setTimeout(60_000));

	    try {
	        FileUtils.writeByteArrayToFile(new File(screenImgPath), screenImg);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    ArtefactObject ao = new ArtefactObject(description, elementImg, new File(screenImgPath), screenImgPath);
	    ThreadUtils.getArtefactRef().add(ao);

	    // Cleanup — only top-level page since overlays are always drawn there
	    page.evaluate("() => { document.querySelectorAll('[data-pw-highlight]').forEach(el => el.remove()); document.documentElement.querySelectorAll('[data-pw-highlight]').forEach(el => el.remove()); }");
	}


	/**
	 * Initializes the ArtefactBuilder by setting necessary objects and flags.
	 */
	protected static void initArtefactBuilder() {
		Boolean custActFlag = true;
		PDFReportObject obj = new PDFReportObject();
		List<ArtefactObject> ao1 = new ArrayList<>();

		setColor(TestData.getElementHighlightColor());

		String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"));
		obj.setStartTime(startTime);

		ThreadUtils.setArtefactRef(ao1);
		ThreadUtils.setPDFReportObj(obj);
		ThreadUtils.setCustFlagRef(custActFlag);
	}

	/**
	 * Generates a DOCX file with artefact screenshots and details.
	 *
	 * @param testClassName the name of the test class
	 * @param startTime     the start time of the test
	 * @param endTime       the end time of the test
	 * @param status        the status of the test
	 * @throws IOException if an I/O error occurs
	 */
	protected static void artefactDocxBuilder(String testClassName, String startTime, String endTime, String status)
			throws IOException {
		if (BaseClass.getParameter(GENERATE_DOCUMENT, "false").equalsIgnoreCase("docx")) {
			DOCXGenerator.generateDOCX(testClassName, startTime, endTime, status);
			File srcFile = new File(ThreadUtils.getPDFReportObj().docxReportFilePath);
			File destFile = new File(System.getProperty("user.dir") + File.separator + "artefact" + File.separator
					+ testClassName + ".docx");
			FileUtils.copyFile(srcFile, destFile);
		}
	}

	/**
	 * Generates a Excel file without artefact screenshots, only with details.
	 * 
	 * @param testClassName the name of the test class
	 * @param startTime     the start time of the test
	 * @param endTime       the end time of the test
	 * @param status        the status of the test
	 * @throws IOException if an I/O error occurs
	 */
	protected static void generateXLS(String testClassName, String startTime, String endTime, String status)
			throws IOException {

		if (BaseClass.getParameter(GENERATE_DOCUMENT, "false").trim().toLowerCase().matches("xls")) {
			DOCXGenerator.writeToXLS(testClassName, startTime, endTime, status);
			File srcFile = new File(ThreadUtils.getPDFReportObj().xlsReportFilePath);
			File destFile = new File(System.getProperty("user.dir") + File.separator + "artefact" + File.separator
					+ testClassName + ".xls");
			FileUtils.copyFile(srcFile, destFile);
		}
	}

	/**
	 * Generates a PDF file with artefact screenshots and details.
	 *
	 * @param testClassName the name of the test class
	 * @param startTime     the start time of the test
	 * @param endTime       the end time of the test
	 * @param status        the status of the test
	 * @throws IOException if an I/O error occurs
	 */
	protected static void artefactPDFBuilder(String testClassName, String startTime, String endTime, String status)
			throws IOException {
		if (BaseClass.getParameter(GENERATE_DOCUMENT, "").equalsIgnoreCase("pdf")) {
			PDFGenerator.generatePDF(testClassName, startTime, endTime, status);
			File srcFile = new File(ThreadUtils.getPDFReportObj().pdfReportFilePath);
			File destFile = new File(System.getProperty("user.dir") + File.separator + "artefact" + File.separator
					+ testClassName + ".pdf");
			FileUtils.copyFile(srcFile, destFile);
		}
	}

	/**
	 * Generates a PDF file with artifact screenshots and details for ATS (Automated
	 * Test Suite).
	 *
	 * @param testClassName the name of the test class
	 * @param startTime     the start time of the test
	 * @param endTime       the end time of the test
	 * @param status        the status of the test
	 */
	@Deprecated
	protected static void artefactPDFBuilderATS(String testClassName, String startTime, String endTime, String status) {
		if (BaseClass.getParameter("Generate_TestResultsDoc", "No").equalsIgnoreCase("yes")) {
			PDFGenerator.generatePDF(testClassName, ThreadUtils.getPDFReportPath(), startTime, endTime, status);
		}
	}

}