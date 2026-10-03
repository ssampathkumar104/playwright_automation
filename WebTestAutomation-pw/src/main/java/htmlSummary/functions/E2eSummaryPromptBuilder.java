package htmlSummary.functions;

import java.io.IOException;

import htmlSummary.contexts.GenAIModelContext;
import htmlSummary.contexts.GenAIPromptContext;
import htmlSummary.contexts.HtmlReportContext.ChildMethod;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;

/** Builds one E2E prompt covering all child scripts of a use case, for a cohesive narrative. */
public class E2eSummaryPromptBuilder {

    /** Prompt template filename, loaded from the project's prompt folder. */
    private static final String PROMPT_FILE = "e2eFunctionalSummaryPrompt.txt";

    /** Placeholder in the template replaced with the use case content. */
    private static final String REPORT_PLACEHOLDER = "{REPORT_CONTENT}";

    /** Max characters of combined context, to prevent token overflow. */
    private static final int MAX_CONTEXT_CHARS = 120_000;

    /** Builds a single prompt for the whole parent use case (all children combined). */
    public static GenAIPromptContext buildParentPrompt(GenAIModelContext model,
                                                       ParentBlock parent) throws IOException {
        String template = GenAIPromptBuilder.readTemplate(PROMPT_FILE);
        String allContext = buildAllChildrenContext(parent);
        String fullPrompt = template.contains(REPORT_PLACEHOLDER)
                ? template.replace(REPORT_PLACEHOLDER, allContext)
                : template + "\n\n<use_case>\n" + allContext + "\n</use_case>\n\nSummary:";
        return GenAIPromptContext.build(model.modelName, model.modelVersion, fullPrompt);
    }

    /** Concatenates all children's context into one string, truncating if it gets too large. */
    private static String buildAllChildrenContext(ParentBlock parent) {
        StringBuilder out = new StringBuilder();
        out.append("Use case: ").append(parent.topLevelName != null ? parent.topLevelName : "Unknown").append("\n");
        out.append("Total scripts: ").append(parent.children.size()).append("\n");
        out.append("Overall status: ").append(parent.status != null ? parent.status.toUpperCase() : "UNKNOWN").append("\n\n");

        for (int i = 0; i < parent.children.size(); i++) {
            ChildMethod child = parent.children.get(i);
            out.append("--- Script ").append(i + 1).append(" ---\n");
            out.append(HtmlDocxExtractor.buildChildContext(child, parent));

            // Guard against token overflow for very large reports
            if (out.length() > MAX_CONTEXT_CHARS) {
                out.setLength(MAX_CONTEXT_CHARS);
                out.append("\n[Context truncated]");
                break;
            }
        }
        return out.toString();
    }
}
