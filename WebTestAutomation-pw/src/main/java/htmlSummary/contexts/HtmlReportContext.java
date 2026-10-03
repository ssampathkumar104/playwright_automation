package htmlSummary.contexts;

import java.util.List;

/** Data model for a parsed ExtentReports HTML file, shared across the htmlSummary module. */
public final class HtmlReportContext {

    private HtmlReportContext() {}

    /** One top-level test block: its children, .docx artifacts, and failure details. */
    public static class ParentBlock {
        public int testId;
        public String status;
        public String topLevelName;
        public List<ChildMethod> children;
        public String failureFunction;
        public String failureMessage;
        public List<DocxArtifact> docs;
    }

    /** One child test method with its script name and status. */
    public static class ChildMethod {
        public String name;
        public String status;
    }

    /** An embedded .docx artifact decoded from the HTML (name + raw bytes). */
    public static class DocxArtifact {
        public String name;
        public byte[] bytes;
    }
}
