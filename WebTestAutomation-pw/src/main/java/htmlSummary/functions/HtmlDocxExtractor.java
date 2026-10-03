package htmlSummary.functions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import htmlSummary.contexts.HtmlParsingConstants;
import htmlSummary.contexts.HtmlReportContext.ChildMethod;
import htmlSummary.contexts.HtmlReportContext.DocxArtifact;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;

/** Parses an ExtentReports HTML file into ParentBlock objects for the summary flow. */
public class HtmlDocxExtractor {

    /** Reads the HTML file and returns one ParentBlock per test-item. */
    public static List<ParentBlock> extractParents(String htmlFilePath) throws IOException {
        String html = new String(Files.readAllBytes(Paths.get(htmlFilePath)), StandardCharsets.UTF_8);
        return parseParents(html);
    }

    /** Builds the plain-text context for one child use case that the LLM reads. */
    public static String buildChildContext(ChildMethod child, ParentBlock parent) {
        StringBuilder out = new StringBuilder();
        out.append("Use case: ").append(child.name).append("\n");
        out.append("Status: ").append(child.status).append("\n");

        // Find the docx artifact matching this child's name
        DocxArtifact doc = null;
        for (DocxArtifact d : parent.docs) {
            if (d.name.contains(child.name)) { doc = d; break; }
        }
        if (doc == null && !parent.docs.isEmpty()) {
            doc = parent.docs.get(0);
        }

        // Decode docx tables into step descriptions
        if (doc != null) {
            try { out.append(decodeDocxTables(doc.bytes)); }
            catch (IOException e) { out.append("[docx decode error]\n"); }
        }

        // Add failure context if this child failed
        if ("Failed".equals(child.status)) {
            if (parent.failureFunction != null) out.append("Failing step: ").append(parent.failureFunction).append("\n");
            if (parent.failureMessage != null) out.append("Failure reason: ").append(parent.failureMessage).append("\n");
        }
        return out.toString();
    }

    /** Parses the full HTML into ParentBlock objects (finds parent blocks, then slices and extracts each). */
    private static List<ParentBlock> parseParents(String html) {
        List<ParentBlock> out = new ArrayList<>();
        List<int[]> spans = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        List<Integer> testIds = new ArrayList<>();

        // Pass 1: Find all parent block positions
        Matcher m = HtmlParsingConstants.PARENT_LI_HEADER.matcher(html);
        while (m.find()) {
            spans.add(new int[] { m.start(), m.end() });
            statuses.add(m.group(1));
            testIds.add(Integer.parseInt(m.group(2)));
        }

        // Pass 2: Slice HTML per parent and extract data
        for (int i = 0; i < spans.size(); i++) {
            int start = spans.get(i)[0];
            int end = (i + 1 < spans.size()) ? spans.get(i + 1)[0] : html.length();
            String blockHtml = html.substring(start, end);

            ParentBlock p = new ParentBlock();
            p.testId = testIds.get(i);
            p.status = statuses.get(i);
            p.topLevelName = firstMatch(blockHtml, HtmlParsingConstants.TOP_LEVEL_NAME, 1);
            p.children = parseChildren(blockHtml);
            p.docs = parseDocxArtifacts(blockHtml);
            if ("fail".equals(p.status)) populateFailureContext(blockHtml, p);
            out.add(p);
        }
        return out;
    }

    /** Extracts child test methods (name + status) from a parent block's HTML slice. */
    private static List<ChildMethod> parseChildren(String blockHtml) {
        List<ChildMethod> children = new ArrayList<>();
        Matcher m = HtmlParsingConstants.TEST_METHOD.matcher(blockHtml);
        while (m.find()) {
            ChildMethod c = new ChildMethod();
            c.name = m.group(1);
            c.status = m.group(2);
            children.add(c);
        }
        return children;
    }

    /** Extracts and decodes base64 .docx artifacts from a parent block's HTML slice. */
    private static List<DocxArtifact> parseDocxArtifacts(String blockHtml) {
        List<DocxArtifact> docs = new ArrayList<>();
        Matcher m = HtmlParsingConstants.DOCX_ARTIFACT.matcher(blockHtml);
        while (m.find()) {
            String name = m.group(1).replace(".docx", "").trim();
            String base64 = m.group(2);
            if (base64.length() < HtmlParsingConstants.MIN_DOCX_BASE64_LENGTH) continue;
            try {
                DocxArtifact d = new DocxArtifact();
                d.name = name;
                d.bytes = Base64.getDecoder().decode(base64);
                docs.add(d);
            } catch (IllegalArgumentException ignored) {}
        }
        return docs;
    }

    /** For failed parents: extracts the failing function name and a short error message. */
    private static void populateFailureContext(String blockHtml, ParentBlock p) {
        Matcher fn = HtmlParsingConstants.FAILING_LN_FUNCTION.matcher(blockHtml);
        if (fn.find()) p.failureFunction = fn.group(2);
        Matcher fs = HtmlParsingConstants.FAILURE_SLICE.matcher(blockHtml);
        if (fs.find()) p.failureMessage = truncate(stripHtml(fs.group(1)), HtmlParsingConstants.FAILURE_MESSAGE_MAX_CHARS);
    }

    /** Decodes a .docx byte array into "label: value" text from its table rows. */
    private static String decodeDocxTables(byte[] bytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    if (cells.size() < 2) continue;
                    String label = cells.get(0).getText().trim();
                    String value = cells.get(1).getText().trim();
                    if (label.isEmpty() || value.isEmpty()) continue;
                    if (label.equalsIgnoreCase("field") && value.equalsIgnoreCase("value")) continue;
                    if (label.matches("\\d+")) sb.append("Step ").append(label).append(": ");
                    else sb.append(label).append(": ");
                    sb.append(value).append("\n");
                }
            }
        }
        return sb.toString();
    }

    /** Returns the first regex match group from the source, or null if not found. */
    private static String firstMatch(String source, Pattern pattern, int group) {
        Matcher m = pattern.matcher(source);
        return m.find() ? m.group(group).trim() : null;
    }

    /** Strips HTML tags and decodes entities to plain text. */
    private static String stripHtml(String s) {
        return s.replaceAll("<[^>]+>", " ").replace("&quot;", "\"").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ")
                .replaceAll("\\s+", " ").trim();
    }

    /** Truncates a string to max characters, appending "..." if truncated. */
    private static String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "...";
    }
}
