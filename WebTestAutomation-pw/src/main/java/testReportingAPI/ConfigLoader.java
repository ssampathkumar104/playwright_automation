package testReportingAPI;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigLoader   {
    private static final Properties envProperties = new Properties();
    /**
     * API-specific logger method to redirect logs to file
     * @param message Message to log
     */
    public static void apiLog(String message) {
        APILoggerConfig.log(message);
    }
    static {
    	
        // Load .env file from various locations
        boolean loaded = false;
        
        // Try first in current directory
        loaded = tryLoadEnvFile(new File(".env"));
        
        // If not loaded, try in project root directory
        if (!loaded) {
            File projectRoot = new File(System.getProperty("user.dir")).getParentFile();
            if (projectRoot != null) {
                loaded = tryLoadEnvFile(new File(projectRoot, ".env"));
            }
        }
        
        // If still not loaded, try two directories up (workspace root)
        if (!loaded) {
            File workspaceRoot = new File(System.getProperty("user.dir")).getParentFile();
            if (workspaceRoot != null && workspaceRoot.getParentFile() != null) {
                loaded = tryLoadEnvFile(new File(workspaceRoot.getParentFile(), ".env"));
            }
        }
        
        if (!loaded) {
            apiLog("ConfigLoader - Warning: .env file not found in any location, will use environment variables");
            // Print current working directory to help diagnose issues
            apiLog("ConfigLoader - Current working directory: " + System.getProperty("user.dir"));
        }
    }
    
    private static boolean tryLoadEnvFile(File envFile) {
        try {
            if (envFile.exists()) {
                try (FileInputStream input = new FileInputStream(envFile)) {
                    envProperties.load(input);
                    apiLog("ConfigLoader - Successfully loaded .env file from: " + envFile.getAbsolutePath());
                    
                    // Debug: print available AWS keys (without showing actual values)
                    if (envProperties.containsKey("AWS_ACCESS_KEY_ID")) {
                        apiLog("ConfigLoader - Found AWS_ACCESS_KEY_ID in .env file");
                    }
                    if (envProperties.containsKey("AWS_ACCESS_SECRET_KEY")) {
                        apiLog("ConfigLoader - Found AWS_ACCESS_SECRET_KEY in .env file");
                    }
                    if (envProperties.containsKey("AWS_STORAGE_BUCKET_NAME")) {
                        apiLog("ConfigLoader - Found AWS_STORAGE_BUCKET_NAME in .env file");
                    }
                    if (envProperties.containsKey("AWS_S3_REGION_NAME")) {
                        apiLog("ConfigLoader - Found AWS_S3_REGION_NAME in .env file");
                    }
                    apiLog("ConfigLoader - Loaded keys from load .env file from ");
                    return true;
                }
            }
        } catch (Exception e) {
        	APILoggerConfig.logError("ConfigLoader - Warning: Failed to load .env file from " + envFile.getAbsolutePath() + ": " + e.getMessage());
        }
        return false;
    }

    public static String getProperty(String key) {
        // First try .env file
        String envValue = envProperties.getProperty(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Then try environment variables
        String envKey = key.toUpperCase().replace('-', '_').replace('.', '_');
        String sysEnvValue = System.getenv(envKey);
        
        if (sysEnvValue != null && !sysEnvValue.isEmpty()) {
            apiLog("Found " + envKey + " in system environment variables");
            return sysEnvValue;
        }
        
        apiLog("ConfigLoader - Property not found: " + key + " (nor as env var: " + envKey + ")");
        return null;
    }
    
    /**
     * Gets a property value with a default fallback if not found
     * @param key Property key to lookup
     * @param defaultValue Default value to return if property is not found
     * @return The property value or default if not found
     */
    @Deprecated
    public static String getPropertyWithDefault(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Check if a feature flag is enabled
     * @param flagName The name of the feature flag (without prefix)
     * @param defaultValue Default value if flag is not set
     * @return true if the feature flag is enabled, false otherwise
     */
    @Deprecated
    public static boolean isFeatureFlagEnabled(String flagName, boolean defaultValue) {
        String fullFlagName = "TESTREPORTING_FF_" + flagName.toUpperCase();
        String value = getProperty(fullFlagName);
        return value != null ? "true".equalsIgnoreCase(value) : defaultValue;
    }

    public static String getAWSAccessKey() {
        return getProperty("AWS_ACCESS_KEY_ID");
    }

    public static String getAWSSecretKey() {
        return getProperty("AWS_ACCESS_SECRET_KEY");
    }

    public static String getAWSRegion() {
        return getProperty("AWS_S3_REGION_NAME");
    }

    public static String getAWSBucketName() {
        return getProperty("AWS_STORAGE_BUCKET_NAME");
    }
    
    public static Properties getEnvProperties() {
        return envProperties;
    }
}
