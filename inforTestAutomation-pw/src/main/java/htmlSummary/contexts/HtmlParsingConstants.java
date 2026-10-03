package htmlSummary.contexts;

import java.util.regex.Pattern;

/** Regex patterns and thresholds used by HtmlDocxExtractor to parse ExtentReports HTML. */
public final class HtmlParsingConstants {

    private HtmlParsingConstants() {}

    /** Matches the opening tag of a parent test block. Captures status (group 1) and test-id (group 2). */
    public static final Pattern PARENT_LI_HEADER = Pattern.compile(
            "<li class=\"test-item\"\\s+status=\"(pass|fail|skip)\"\\s+test-id=\"(\\d+)\"", Pattern.DOTALL);

    /** Matches the human-readable class name at the top of a parent block. Captures name (group 1). */
    public static final Pattern TOP_LEVEL_NAME = Pattern.compile("<p class=\"name\">([^<]+)</p>");

    /** Matches child test method results within a parent block. Captures name (group 1) and status (group 2). */
    public static final Pattern TEST_METHOD = Pattern.compile("Test Method:\\s*(\\w+)\\s+is\\s+(Passed|Failed|Skipped)");

    /** Matches the LN business function at the top of a failure stack trace. Captures class (group 1) and method (group 2). */
    public static final Pattern FAILING_LN_FUNCTION = Pattern.compile("standard\\.ln\\.\\w+\\.functions\\.(\\w+)\\.(\\w+)\\(");

    /** Captures the failure details between "is Failed." and the next download link. Captures message (group 1). */
    public static final Pattern FAILURE_SLICE = Pattern.compile("Test Method:\\s*\\w+\\s+is Failed\\.(.*?)Click to download", Pattern.DOTALL);

    /** Matches embedded base64-encoded .docx artifacts in the HTML. Captures name (group 1) and base64 (group 2). */
    public static final Pattern DOCX_ARTIFACT = Pattern.compile("Click to download '([^']+)'\\s+use Case document.*?application/docx;base64,([A-Za-z0-9+/=]+)", Pattern.DOTALL);

    /** Minimum base64 length to be considered a real docx (skips empty stubs). */
    public static final int MIN_DOCX_BASE64_LENGTH = 10_000;

    /** Maximum characters for failure message passed to the model. */
    public static final int FAILURE_MESSAGE_MAX_CHARS = 400;
}
