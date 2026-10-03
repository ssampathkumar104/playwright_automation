package htmlSummary.functions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import htmlSummary.config.SummaryConfigLoader;
import htmlSummary.contexts.GenAIModelContext;
import htmlSummary.contexts.HtmlReportContext.ParentBlock;

/**
 * Entry point for GenAI HTML report summarization: one report (real plan flow)
 * or a batch of reports (standalone).
 */
public class GenAIReportFunctions {

    private static final Logger log = LogManager.getLogger(GenAIReportFunctions.class);

    /** Output folder name; defaults to "html-summary". */
    private static final String OUTPUT_DIR = SummaryConfigLoader.getOutputFolder();

    /** Authenticates, parses one HTML report, and saves its summary .docx (real plan flow). */
    public static void generateSummaryForReport(String htmlReportPath) throws Exception {
        // Authenticate with ION Gateway (ONCE per plan)
        GenAIService service = GenAIService.authenticate();

        // Select the best available GenAI model (ONCE per plan)
        GenAIModelContext model = service.selectBestModel();
        log.info("====== [GenAI Summary] Model: " + model + " ======");

        // Create the output directory
        String outputDir = prepareOutputDirectory();

        // Extract the report filename from the full path
        String fileName = getFileName(htmlReportPath);

        // Build the summary output path
        String outputPath = buildOutputPath(outputDir, fileName);

        // Parse HTML into structured parent blocks
        log.info("====== [GenAI Summary] Processing: " + fileName + " ======");
        List<ParentBlock> parents = HtmlDocxExtractor.extractParents(htmlReportPath);
        log.info("====== [GenAI Summary] Parsed " + parents.size() + " parent use cases ======");

        // Generate summary DOCX (loops through each use case, calls LLM)
        SummaryReportGenerator.generate(service, model, parents, fileName, outputPath);
        log.info("====== [GenAI Summary] Saved: " + outputPath + " ======");
    }

    /** Discovers and summarizes all HTML reports (standalone/batch mode). */
    public static void generateAllSummaryReports(GenAIService service,
                                                  GenAIModelContext model) throws Exception {
        // Discover which HTML reports to process
        List<String> reports = resolveInputReports();
        String outputDir = prepareOutputDirectory();

        for (String htmlPath : reports) {
            String fileName = getFileName(htmlPath);
            log.info("====== [GenAI Summary] Processing: " + fileName + " ======");
            try {
                List<ParentBlock> parents = HtmlDocxExtractor.extractParents(htmlPath);
                log.info("====== [GenAI Summary] Parsed " + parents.size() + " parent use cases ======");
                String outputPath = buildOutputPath(outputDir, fileName);
                SummaryReportGenerator.generate(service, model, parents, fileName, outputPath);
                log.info("====== [GenAI Summary] Saved: " + outputPath + " ======");
            } catch (Exception e) {
                log.error("====== [GenAI Summary] ERROR processing " + fileName + ": " + e.getMessage() + " ======", e);
            }
        }
        log.info("====== [GenAI Summary] All reports processed ======");
    }

    /** Resolves reports to process: -DSINGLE_REPORT_PATH, then -DREPORT_INPUT_FOLDER, then Reports/. */
    private static List<String> resolveInputReports() {
        // Priority 1: Single file mode (for local testing with one report)
        String singlePath = System.getProperty("SINGLE_REPORT_PATH", "");
        if (singlePath != null && !singlePath.trim().isEmpty()) {
            Path single = Paths.get(singlePath).toAbsolutePath();
            if (!Files.exists(single)) {
                throw new IllegalStateException("SINGLE_REPORT_PATH does not exist: " + single);
            }
            log.info("====== [GenAI Summary] Single-file mode: " + single + " ======");
            return Collections.singletonList(single.toString());
        }

        // Priority 2: Folder override (Jenkins can pass custom folder)
        String folderOverride = System.getProperty("REPORT_INPUT_FOLDER", "");
        if (folderOverride != null && !folderOverride.trim().isEmpty()) {
            List<String> reports = getHtmlReportsFromFolder(folderOverride);
            log.info("====== [GenAI Summary] Folder mode: " + reports.size() + " reports in " + folderOverride + " ======");
            return reports;
        }

        // Priority 3: Default Reports/ folder
        List<String> reports = getHtmlReportsFromFolder("Reports");
        log.info("====== [GenAI Summary] Reports/ mode: " + reports.size() + " reports found ======");
        return reports;
    }

    /** Returns .html files in a folder, excluding GenAI's own output reports. */
    private static List<String> getHtmlReportsFromFolder(String folderName) {
        List<String> htmlFiles = new ArrayList<>();
        Path folder = Paths.get(folderName).toAbsolutePath();
        if (!Files.exists(folder)) return htmlFiles;
        try {
            Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".html"))
                    .filter(p -> !isExcludedReport(p.getFileName().toString()))
                    .forEach(p -> htmlFiles.add(p.toAbsolutePath().toString()));
        } catch (IOException e) {
            log.error("====== [GenAI Summary] Error reading folder " + folder + ": " + e.getMessage() + " ======", e);
        }
        return htmlFiles;
    }

    /** Excludes GenAI's own output and API test reports (prevents summarizing a summary). */
    private static boolean isExcludedReport(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.startsWith("genaillmservice")
                || lower.startsWith("genaiee2esummary")
                || lower.startsWith("salesorderapi");
    }

    /** Creates the output directory if needed and returns its absolute path. */
    private static String prepareOutputDirectory() throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR).toAbsolutePath();
        Files.createDirectories(outputDir);
        return outputDir.toString();
    }

    /** Extracts just the filename from a full path. */
    private static String getFileName(String htmlPath) {
        return Paths.get(htmlPath).getFileName().toString();
    }

    /** Builds the summary output path: strips the date-time suffix and adds "Summary_" prefix. */
    private static String buildOutputPath(String outputDir, String inputHtmlFileName) {
        String outputFileName = "Summary_" + inputHtmlFileName
                .replaceAll(SummaryDocxBuilder.DATE_TIME_SUFFIX_REGEX, "")
                .replace(".html", ".docx");
        return Paths.get(outputDir).resolve(outputFileName).toString();
    }
}
