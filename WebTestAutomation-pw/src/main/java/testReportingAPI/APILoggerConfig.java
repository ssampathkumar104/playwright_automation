package testReportingAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import testBase.BaseClass;

/**
 * Custom logger configuration for API-related logs
 * Redirects specific API logs to a separate file
 */
class APILoggerConfig extends BaseClass {
//	private static String DEFAULT_LOG_FILENAME = "S3Log_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))+".txt";
	private static String DEFAULT_LOG_FILENAME = "S3Log.log";
    private static FileHandler fileHandler;
    private static Logger apiLogger;
    private static boolean initialized = false;

    // Flag to control console logging
    private static boolean enableConsoleLogging = false;
    
    static {
        initialize();
    }
    
    /**
     * Initialize the API logger with file handler
     */
    private static void initialize() {
        try {
			if (initialized)
				return;
            
			// Configure the logger
            apiLogger = Logger.getLogger("API_LOGGER");
            apiLogger.setUseParentHandlers(false);  
    		
            // Create file handler with append set to true
            fileHandler = new FileHandler(DEFAULT_LOG_FILENAME, true);
            fileHandler.setFormatter(new CustomLogFormatter());
            apiLogger.addHandler(fileHandler);
            
         // Optionally add console logging
            if (enableConsoleLogging) {
            	ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new CustomLogFormatter());
                apiLogger.addHandler(consoleHandler);
            }
           
            apiLogger.info("API Logger initialized - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            initialized = true;
        } catch (IOException e) {
        	System.err.println("Failed to initialize API logger: " + e.getMessage());
        	e.printStackTrace();
        }
    }
    
//    /**
//     * Get the configured API logger instance
//     * @return Logger instance for API logs
//     */
//    @Deprecated
//    public static Logger getAPILogger() {
//        if (!initialized) {
//            initialize();
//        }
//        return apiLogger;
//    }
//    
    /**
     * Log API-related message
     * @param message Message to log
     */
    protected static void log(String message) {
    	if (!initialized) {
            initialize();
        }
        apiLogger.info(message);
    }
    
    /**
     * Log API-related error
     * @param message Error message to log
     * @param e Exception that occurred
     */
    protected static void logError(String message, Exception e) {
    	if (!initialized) {
            initialize();
        }
        apiLogger.severe(message + ": \n" + e.getMessage());
    }
    
    /**
     * Log API-related error
     * @param message Error message to log
     */
    protected static void logError(String message) {
    	if (!initialized) {
            initialize();
        }
        apiLogger.severe(message );
    }
    
    protected static void logWarn(String message) {
    	if (!initialized) {
            initialize();
        }
        apiLogger.log(Level.WARNING, message );
    }
    
    /**
     * Close the file handler to release resources
     */
    public static void closeLogger() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
    
    protected static String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}