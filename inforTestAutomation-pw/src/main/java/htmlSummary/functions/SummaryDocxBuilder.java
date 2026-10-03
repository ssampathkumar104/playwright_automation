package htmlSummary.functions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import htmlSummary.contexts.HtmlReportContext.ChildMethod;
import htmlSummary.contexts.HtmlReportContext.DocxArtifact;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;

/** Assembles the summary Word document (.docx) in memory and saves it via saveAndClose(). */
public class SummaryDocxBuilder implements AutoCloseable {

    /** Regex to strip date-time suffix from filenames. */
    public static final String DATE_TIME_SUFFIX_REGEX =
            "_\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}";

    /** Hex color for failed content. */
    private static final String COLOR_RED = "FF0000";

    /** Hex color for skipped content (dark/amber yellow — readable on white). */
    private static final String COLOR_YELLOW = "BF8F00";

    private final XWPFDocument doc;

    public SummaryDocxBuilder() {
        this.doc = new XWPFDocument();
    }

    /** Writes document title and generation date. */
    public void writeTitle(String htmlFileName, String today) {
        String planName = htmlFileName
                .replaceAll(DATE_TIME_SUFFIX_REGEX, "")
                .replace(".html", "");
        addParagraph(planName + " - Summary Report", true, 16, false);
        addParagraph("Generated: " + today, false, 11, false);
        doc.createParagraph();
    }

    /** Writes E2E-style executive summary with pipe separators. */
    public void writeE2eExecutiveSummary(Counts counts, int totalParents) {
        addParagraph("Executive Summary:", true, 14, false);
        String result = counts.parentFail > 0 ? "FAILED"
                : (counts.parentSkip == totalParents ? "SKIPPED" : "PASSED");
        addParagraph("Total Use Cases: " + totalParents
                + "    |    Total Scripts Executed: "
                + (counts.childPass + counts.childFail)
                + "    |    Result: " + result, false, 11, false);
        doc.createParagraph();
    }

    /** Writes Module-style itemized executive summary. */
    public void writeModuleExecutiveSummary(Counts counts, int totalParents) {
        addParagraph("Executive Summary:", true, 14, false);
        addParagraph("- Total Use Cases: " + totalParents, false, 11, false);
        addParagraph("- Passed: " + counts.parentPass, false, 11, false);
        addParagraph("- Failed: " + counts.parentFail, false, 11, false);
        if (counts.parentSkip > 0) {
            addParagraph("- Skipped: " + counts.parentSkip, false, 11, false);
        }
        doc.createParagraph();
    }

    /** Writes an E2E use case heading. */
    public void writeE2eUseCaseHeading(String name) {
        addParagraph("Use Case - " + name, true, 13, false);
    }

    /** Writes the E2E bullet narrative; failed lines red, skipped use cases yellow. */
    public void writeBulletNarrative(String narrative, boolean isFailed, boolean isSkipped) {
        for (String line : narrative.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            boolean isFailLine = isFailed && line.toLowerCase().contains("failed");
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.LEFT);
            p.setIndentationLeft(360);
            XWPFRun run = p.createRun();
            run.setText(line);
            run.setFontSize(11);
            if (isFailLine) run.setColor(COLOR_RED);
            else if (isSkipped) run.setColor(COLOR_YELLOW);
        }
        doc.createParagraph();
    }

    /** Writes a Module use case heading. */
    public void writeModuleUseCaseHeading(String label, String scriptName) {
        addParagraph(label + ": " + scriptName, true, 14, false);
    }

    /** Writes the Module summary; failure sentence red, whole summary yellow if skipped. */
    public void writeModuleSummary(String summary, boolean isFailed, boolean isSkipped) {
        if (isSkipped) {
            addParagraphColor(summary, false, 11, COLOR_YELLOW);
        } else if (isFailed && summary.contains("failed")) {
            int failIdx = summary.lastIndexOf(" failed");
            if (failIdx < 0) failIdx = summary.lastIndexOf("Failed");
            if (failIdx > 0) {
                int sentStart = summary.lastIndexOf(". ", failIdx);
                sentStart = (sentStart >= 0) ? sentStart + 2 : 0;
                String passedPart = summary.substring(0, sentStart).trim();
                String failedPart = summary.substring(sentStart).trim();
                if (!passedPart.isEmpty()) addParagraph(passedPart, false, 11, false);
                addParagraph(failedPart, false, 11, true);
            } else {
                addParagraph(summary, false, 11, true);
            }
        } else {
            addParagraph(summary, false, 11, false);
        }
        doc.createParagraph();
    }

    /** Saves the DOCX to disk (creating parent dirs) and closes it. */
    public void saveAndClose(String outputPath) throws IOException {
        Files.createDirectories(Paths.get(outputPath).getParent());
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            doc.write(fos);
        }
        doc.close();
    }

    /** Closes the document without saving (used on exception). */
    @Override
    public void close() throws IOException { doc.close(); }

    /** Resolves the full script name for Module headings (docx artifact name, else child.name). */
    public static String resolveFullName(ChildMethod child, ParentBlock parent) {
        for (DocxArtifact d : parent.docs) {
            if (d.name != null && d.name.startsWith(child.name)) return d.name;
        }
        if (parent.children.size() == 1 && parent.topLevelName != null
                && parent.topLevelName.contains(child.name)) {
            return parent.topLevelName;
        }
        return child.name;
    }

    /** Counts pass/fail/skip at parent and child levels (all-skipped→skip, any-fail→fail, else pass). */
    public static Counts tally(List<ParentBlock> parents) {
        Counts c = new Counts();
        for (ParentBlock p : parents) {
            boolean anyFailed = false, allSkipped = true;
            for (ChildMethod ch : p.children) {
                if ("Failed".equals(ch.status)) { anyFailed = true; allSkipped = false; c.childFail++; }
                if ("Passed".equals(ch.status)) { allSkipped = false; c.childPass++; }
                if ("Skipped".equalsIgnoreCase(ch.status)) c.childSkip++;
                else allSkipped = false;
            }
            if (allSkipped || p.children.isEmpty()) c.parentSkip++;
            else if (anyFailed) c.parentFail++;
            else c.parentPass++;
        }
        return c;
    }

    /** Holds pass/fail/skip counts at parent and child levels. */
    public static class Counts {
        public int parentPass, parentFail, parentSkip;
        public int childPass, childFail, childSkip;
    }

    /** Adds a styled paragraph to the document. Used by all write methods. */
    private void addParagraph(String text, boolean bold, int fontSize, boolean red) {
        addParagraphColor(text, bold, fontSize, red ? COLOR_RED : null);
    }

    /** Adds a styled paragraph with an explicit hex color (null = default black). */
    private void addParagraphColor(String text, boolean bold, int fontSize, String colorHex) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
        if (colorHex != null) run.setColor(colorHex);
    }
}
