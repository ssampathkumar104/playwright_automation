package testBase.listners;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import dataUtils.RuntimeData;
import testBase.ThreadUtils;

public class LogFormatter {

    private LogFormatter() {
    }

    private static String pattern = "[%-5p] %d{yyyy-MM-dd HH:mm:ss.SSS} - %m%n";

    public static void initLogFormatter(String testClassName) {
    	// Create a logger specific to the test class
    	String tc = testClassName + RuntimeData.getRandomChars(12);
    	Logger logger = LogManager.getLogger(tc);
    	
		// Get the LoggerContext and Configuration
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		// Disable the root logger to prevent external logs
		config.getRootLogger().setLevel(Level.OFF);

		// Create and set LoggerConfig for your own package to DEBUG or INFO
		LoggerConfig appLoggerConfig = config.getLoggerConfig(tc); 

		appLoggerConfig = new LoggerConfig(tc, Level.DEBUG, false);
		config.addLogger(tc, appLoggerConfig);

	    // Remove any existing console appenders to avoid duplicate logs
	    config.getRootLogger().getAppenders().forEach((name, appender) -> {
	        if (appender instanceof ConsoleAppender) {
	            config.getLoggerConfig(logger.getName()).removeAppender(name);
	        }
	    });

		// Create a pattern layout for log formatting
		PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();

    	// Create and configure the console appender for your specific logger
    	ConsoleAppender consoleAppender = ConsoleAppender.newBuilder()
    	        .setName("ConsoleLogger")
    	        .setLayout(layout)
    	        .build();
    	consoleAppender.start();
    	
    	LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
	    loggerConfig.setLevel(Level.DEBUG);

    	// Create the file paths for the log files
    	String filePath = ThreadUtils.getTempDirectoryPath() + File.separator + testClassName.trim() + File.separator + "log" + File.separator + testClassName.trim();
    	String actionLogFile = filePath + "_actionLog.txt";
    	String detailedLogFile = filePath + "_detailedLog.txt";

    	// Create and configure the action log file appender
    	FileAppender actionLogAppender = FileAppender.newBuilder()
    	        .setName("ActionFileLogger")
    	        .withFileName(actionLogFile)
    	        .setLayout(layout)
    	        .withAppend(true)
    	        .build();
    	actionLogAppender.start();

    	// Create and configure the detailed log file appender
    	FileAppender detailedLogAppender = FileAppender.newBuilder()
    	        .setName("DetailedFileLogger")
    	        .withFileName(detailedLogFile)
    	        .setLayout(layout)
    	        .withAppend(true)
    	        .build();
    	detailedLogAppender.start();

    	// Add appenders to your logger (not root)
    	loggerConfig.addAppender(consoleAppender, Level.DEBUG, null);
    	loggerConfig.addAppender(detailedLogAppender, Level.DEBUG, null);
    	loggerConfig.addAppender(actionLogAppender, Level.INFO, new MyFilter());
    	

    	// Update the logger context with the new configuration
    	context.updateLoggers();

    	// Setting the logger for the current thread
    	ThreadUtils.setLogger(logger);

    }
    
    /**
     * Creates the necessary directories for download, log, and screenshot files.
     */
    public static void createDownloadLogScreenshotDirectories(String name) {
        String path = null;
        try {
            for (String s : Arrays.asList("download", "log", "screenshot", "artefact")) {
                path = ThreadUtils.getTempDirectoryPath() + name + File.separator + s;
                File fname = new File(path);
                if (!fname.exists()) {
                    fname.mkdirs();
                }

                if (s.equalsIgnoreCase("screenshot")) {
                    LinkedList<List<String>> ssObject = new LinkedList<>();
                    ThreadUtils.setSSObjRef(ssObject);
                    ThreadUtils.setScreenshotDirectoryPath(path);
                }
            }
            System.out.println(
                    "Created 'download', 'log', and 'screenshot' folders at " + ThreadUtils.getTempDirectoryPath());
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public static void closeAllAppenders() {
        try {
            // Get the current logger context
        	LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();

            // Iterate over all configured loggers in the context
            config.getLoggers().forEach((loggerName, loggerConfig) -> {
                // Stop all appenders for each logger
                loggerConfig.getAppenders().forEach((name, appender) -> {
                    appender.stop();  // This will close file handles
                    loggerConfig.removeAppender(name);  // Remove appender from the logger config
                });
            });

            // Also stop appenders for the root logger, if necessary
            LoggerConfig rootLoggerConfig = config.getRootLogger();
            rootLoggerConfig.getAppenders().forEach((name, appender) -> {
                appender.stop();  // Stop and close appenders for the root logger
                rootLoggerConfig.removeAppender(name);
            });

            // Update the context to apply the changes
            context.updateLoggers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

class MyFilter extends AbstractFilter {
    @Override
    public Result filter(org.apache.logging.log4j.core.LogEvent event) {
        if (event.getLevel() == Level.INFO) {
            return Result.ACCEPT;
        }
        return Result.DENY;
    }
}
