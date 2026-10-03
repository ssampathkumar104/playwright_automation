package htmlSummary.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import testBase.BaseClass;

/**
 * Config helper for GenAI summary: resolves credentials, prompt folder, and
 * output folder used by the summary generation flow.
 */
public class SummaryConfigLoader {

    private static final Logger log = LogManager.getLogger(SummaryConfigLoader.class);
    private static final Properties envProperties = new Properties();
    private static boolean loaded = false;
    /** Absolute path of the genai-summary.env that was loaded, or null if none found. */
    private static String loadedEnvPath = null;

    /** Prompt template filenames required for summary generation. */
    private static final String[] REQUIRED_PROMPTS = {
        "functionalSummaryPrompt.txt",   // Module reports
        "e2eFunctionalSummaryPrompt.txt" // E2E reports
    };

    /** Loads genai-summary.env on first access. */
    static {
        loadEnvFile();
    }

    /** Returns a property value, checking -D, then TestNG XML, then genai-summary.env. */
    public static String getProperty(String key) {
        // 1. System property (highest priority — Jenkins -D flags)
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isEmpty()) {
            return stripQuotes(sysValue);
        }

        // 2. TestNG XML parameter
        try {
            String testngValue = BaseClass.getParameter(key);
            if (testngValue != null && !testngValue.isEmpty()) {
                return stripQuotes(testngValue);
            }
        } catch (Exception e) {
            // TestNG context may not be available
        }

        // 3. genai-summary.env file
        String envValue = envProperties.getProperty(key);
        if (envValue != null && !envValue.isEmpty()) {
            return stripQuotes(envValue);
        }

        return null;
    }

    /** Returns a property value, or the given default if not found anywhere. */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /** Prompt templates folder for the consuming project; defaults to "genai-prompts". */
    public static String getPromptFolder() {
        return getProperty("summaryPromptFolder", "genai-prompts");
    }

    /** Resolved prompt folder as an absolute path (against the project working dir). */
    public static String getPromptFolderAbsolutePath() {
        return new File(System.getProperty("user.dir") + File.separator + getPromptFolder()).getAbsolutePath();
    }

    /** True if the resolved prompt folder exists on disk. */
    public static boolean isPromptFolderAvailable() {
        return new File(getPromptFolderAbsolutePath()).exists();
    }

    /** True if the prompt folder exists and contains all required prompt templates. */
    public static boolean arePromptsAvailable() {
        String folder = getPromptFolderAbsolutePath();
        if (!new File(folder).exists()) {
            return false;
        }
        for (String prompt : REQUIRED_PROMPTS) {
            if (!new File(folder, prompt).exists()) {
                return false;
            }
        }
        return true;
    }

    /** Output folder for summary files; defaults to "Reports" (alongside the HTML/CSV reports). */
    public static String getOutputFolder() {
        return getProperty("summaryOutputFolder", "Reports");
    }

    /** Resolved output folder as an absolute path (against the project working dir). */
    public static String getOutputFolderAbsolutePath() {
        return new File(System.getProperty("user.dir") + File.separator + getOutputFolder()).getAbsolutePath();
    }

    /** True if a genai-summary.env file was found and loaded. */
    public static boolean isEnvFileLoaded() {
        return loaded;
    }

    /** Absolute path of the loaded genai-summary.env, or null if none was found. */
    public static String getLoadedEnvFilePath() {
        return loadedEnvPath;
    }

    /** True if all credentials required to authenticate with the GenAI service are present. */
    public static boolean hasRequiredCredentials() {
        String[] required = {
            "BEARER_BASE_URI", "CLIENT_ID", "CLIENT_SECRET_ID",
            "TOKEN_GENERATION_USERNAME", "TOKEN_GENERATION_PASSWORD",
            "XTENANT_ID", "RESOURCE_BASE_URL"
        };
        for (String key : required) {
            String v = getProperty(key);
            if (v == null || v.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Loads genai-summary.env from the project root or parent directory. */
    private static void loadEnvFile() {
        if (loaded) return;

        String[] candidates = {
            "genai-summary.env",
            System.getProperty("user.dir") + File.separator + "genai-summary.env",
        };

        // Also check parent directory (workspace root for multi-module projects)
        File parentDir = new File(System.getProperty("user.dir")).getParentFile();
        if (parentDir != null) {
            candidates = appendPath(candidates,
                    parentDir.getAbsolutePath() + File.separator + "genai-summary.env");
        }

        for (String path : candidates) {
            if (tryLoadEnvFile(new File(path))) {
                loaded = true;
                return;
            }
        }

        log.info("[GenAI Summary] No genai-summary.env found. "
                + "Will use -D system properties or TestNG parameters.");
    }

    /** Loads properties from a single file; returns true if it existed and loaded. */
    private static boolean tryLoadEnvFile(File envFile) {
        try {
            if (envFile.exists()) {
                try (FileInputStream input = new FileInputStream(envFile)) {
                    envProperties.load(input);
                    loadedEnvPath = envFile.getAbsolutePath();
                    log.info("[GenAI Summary] Loaded config from: "
                            + envFile.getAbsolutePath());
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("[GenAI Summary] Warning: Failed to load "
                    + envFile.getAbsolutePath() + ": " + e.getMessage());
        }
        return false;
    }

    /** Strips surrounding single or double quotes from a value. */
    private static String stripQuotes(String value) {
        if (value == null) return null;
        value = value.trim();
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /** Appends one path to a String array. */
    private static String[] appendPath(String[] original, String newPath) {
        String[] result = new String[original.length + 1];
        System.arraycopy(original, 0, result, 0, original.length);
        result[original.length] = newPath;
        return result;
    }
}
