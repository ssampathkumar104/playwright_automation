package htmlSummary.functions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import htmlSummary.contexts.GenAIModelContext;
import htmlSummary.contexts.GenAIPromptContext;
import htmlSummary.contexts.HtmlReportContext.ChildMethod;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;
import htmlSummary.functions.SummaryDocxBuilder.Counts;
import htmlSummary.retry.RetryExecutor;
import io.restassured.response.Response;

/** Generates the summary .docx from parsed report data (detects E2E vs Module, calls LLM per use case). */
public class SummaryReportGenerator {

    private static final Logger log = LogManager.getLogger(SummaryReportGenerator.class);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /** E2E reports start with the "E2E_" prefix; everything else is treated as Module. */
    private static final Pattern E2E_REPORT_PATTERN =
            Pattern.compile("^E2E_.*", Pattern.CASE_INSENSITIVE);

    /** Builds and saves the summary .docx: detects type, writes title/summary, loops use cases. */
    public static void generate(GenAIService service,
                                GenAIModelContext model,
                                List<ParentBlock> parents,
                                String htmlFileName,
                                String outputPath) throws Exception {
        long startTime = System.currentTimeMillis();
        String today = LocalDate.now().format(DATE_FORMAT);

        // Detect report type from filename prefix
        boolean isE2e = isE2eReport(htmlFileName);

        // Create DOCX document (AutoCloseable — saved on exit)
        try (SummaryDocxBuilder builder = new SummaryDocxBuilder()) {

            // Count pass/fail/skip at parent and child levels
            Counts counts = SummaryDocxBuilder.tally(parents);

            // Write document title and generation date
            builder.writeTitle(htmlFileName, today);

            // Write executive summary and run appropriate summarization loop
            if (isE2e) {
                // E2E: one prompt per parent (all children sent together as one narrative)
                builder.writeE2eExecutiveSummary(counts, parents.size());
                summarizeE2e(service, model, builder, parents, htmlFileName);
            } else {
                // Module: one prompt per child script (individual summaries)
                builder.writeModuleExecutiveSummary(counts, parents.size());
                summarizeModule(service, model, builder, parents);
            }

            // Save the DOCX file to disk
            builder.saveAndClose(outputPath);
        }

        log.info("====== [GenAI Summary] Report generated in " + formatDuration(System.currentTimeMillis() - startTime) + " ======");
    }

    /** E2E summarization: one prompt per parent (all children combined into one narrative). */
    private static void summarizeE2e(GenAIService service,
                                     GenAIModelContext model,
                                     SummaryDocxBuilder builder,
                                     List<ParentBlock> parents,
                                     String htmlFileName) {
        for (ParentBlock parent : parents) {
            // Derive use case name from filename or parent's topLevelName
            String useCaseName = htmlFileName
                    .replaceAll(SummaryDocxBuilder.DATE_TIME_SUFFIX_REGEX, "")
                    .replace(".html", "");
            if (parents.size() > 1 && parent.topLevelName != null) {
                useCaseName = parent.topLevelName;
            }

            builder.writeE2eUseCaseHeading(useCaseName);
            String narrative = callE2ePrompt(service, model, parent);
            builder.writeBulletNarrative(narrative, "fail".equals(parent.status), "skip".equals(parent.status));
        }
    }

    /** Calls the E2E prompt; returns the narrative or fallback text on failure. */
    private static String callE2ePrompt(GenAIService service, GenAIModelContext model, ParentBlock parent) {
        if ("skip".equals(parent.status)) {
            return "\u00B7 This use case was skipped. No activities were executed.";
        }
        try {
            GenAIPromptContext promptCtx = E2eSummaryPromptBuilder.buildParentPrompt(model, parent);
            Response response = RetryExecutor.withRetries(() -> service.postPrompt(promptCtx));
            if (response.getStatusCode() != 200) {
                return "\u00B7 Summary unavailable (API error " + response.getStatusCode() + ").";
            }
            String content = response.jsonPath().getString("content");
            return (content != null && !content.trim().isEmpty())
                    ? content.trim()
                    : "\u00B7 Summary unavailable (empty response).";
        } catch (Exception e) {
            return "\u00B7 Summary unavailable (" + e.getMessage() + ").";
        }
    }

    /** Module summarization: one prompt per child script, labelled "Use Case 1", "4.a", etc. */
    private static void summarizeModule(GenAIService service,
                                        GenAIModelContext model,
                                        SummaryDocxBuilder builder,
                                        List<ParentBlock> parents) {
        int parentIndex = 1;
        for (ParentBlock parent : parents) {
            boolean isDependent = parent.children.size() > 1;
            int letterIndex = 0;

            for (ChildMethod child : parent.children) {
                // Build label: "Use Case 1" or "Use Case 4.a" for dependency chains
                String label = isDependent
                        ? "Use Case " + parentIndex + "." + (char)('a' + letterIndex)
                        : "Use Case " + parentIndex;

                // Resolve full script name from docx artifact name
                String fullName = SummaryDocxBuilder.resolveFullName(child, parent);

                // Write heading and get LLM summary
                builder.writeModuleUseCaseHeading(label, fullName);
                String summary = callModulePrompt(service, model, child, parent);
                builder.writeModuleSummary(summary, "Failed".equalsIgnoreCase(child.status), "Skipped".equalsIgnoreCase(child.status));

                letterIndex++;
            }
            parentIndex++;
        }
    }

    /** Calls the Module prompt for one child; returns the summary or fallback text on failure. */
    private static String callModulePrompt(GenAIService service, GenAIModelContext model,
                                            ChildMethod child, ParentBlock parent) {
        if ("Skipped".equalsIgnoreCase(child.status)) {
            return "This use case was skipped. No activities were executed.";
        }
        try {
            GenAIPromptContext promptCtx = GenAIPromptBuilder.buildChildPrompt(model, child, parent);
            Response response = RetryExecutor.withRetries(() -> service.postPrompt(promptCtx));
            if (response.getStatusCode() != 200) {
                return "Summary unavailable (API error " + response.getStatusCode() + ").";
            }
            String content = response.jsonPath().getString("content");
            return (content != null && !content.trim().isEmpty())
                    ? content.trim()
                    : "Summary unavailable (empty response).";
        } catch (Exception e) {
            return "Summary unavailable (" + e.getMessage() + ").";
        }
    }

    /** True if the filename has the "E2E_" prefix (E2E report); else Module. */
    private static boolean isE2eReport(String htmlFileName) {
        String baseName = htmlFileName
                .replaceAll(SummaryDocxBuilder.DATE_TIME_SUFFIX_REGEX, "")
                .replace(".html", "");
        return E2E_REPORT_PATTERN.matcher(baseName).matches();
    }

    /** Formats milliseconds into a human-readable duration (e.g. "2m 15s"). */
    private static String formatDuration(long ms) {
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long remainSec = seconds % 60;
        if (minutes < 60) return minutes + "m " + remainSec + "s";
        long hours = minutes / 60;
        long remainMin = minutes % 60;
        return hours + "h " + remainMin + "m " + remainSec + "s";
    }
}
