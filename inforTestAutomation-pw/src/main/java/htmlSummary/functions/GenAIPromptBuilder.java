package htmlSummary.functions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import htmlSummary.config.SummaryConfigLoader;
import htmlSummary.contexts.GenAIModelContext;
import htmlSummary.contexts.GenAIPromptContext;
import htmlSummary.contexts.HtmlReportContext.ChildMethod;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;

/** Builds Module-style prompts, one per child script, each producing a short summary. */
public class GenAIPromptBuilder {

    /** Prompt template filename, loaded from the project's prompt folder. */
    private static final String PROMPT_FILE = "functionalSummaryPrompt.txt";

    /** Placeholder in the template replaced with the use case content. */
    private static final String REPORT_PLACEHOLDER = "{REPORT_CONTENT}";

    /** Builds a prompt for a single child use case. */
    public static GenAIPromptContext buildChildPrompt(GenAIModelContext model,
                                                      ChildMethod child,
                                                      ParentBlock parent) throws IOException {
        String template = readTemplate(PROMPT_FILE);
        String context = HtmlDocxExtractor.buildChildContext(child, parent);
        String fullPrompt = template.contains(REPORT_PLACEHOLDER)
                ? template.replace(REPORT_PLACEHOLDER, context)
                : template + "\n\n<use_case>\n" + context + "\n</use_case>\n\nSummary:";
        return GenAIPromptContext.build(model.modelName, model.modelVersion, fullPrompt);
    }

    /** Loads a prompt template from the project's prompt folder; throws if it is missing. */
    static String readTemplate(String fileName) throws IOException {
        Path projectPrompt = Paths.get(SummaryConfigLoader.getPromptFolder(), fileName).toAbsolutePath();
        if (Files.exists(projectPrompt)) {
            return new String(Files.readAllBytes(projectPrompt), StandardCharsets.UTF_8).trim();
        }
        throw new IOException("Prompt template '" + fileName + "' not found in project prompt folder: "
                + projectPrompt + ". Add it to the '" + SummaryConfigLoader.getPromptFolder()
                + "' folder in your project root.");
    }
}
