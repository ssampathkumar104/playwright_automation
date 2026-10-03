package testReportingAPI;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {
    private static final DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        String time = timestampFormat.format( LocalDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), ZoneId.systemDefault()));
        
        String level = String.format("%-5s", record.getLevel().getName());
        String message = formatMessage(record);
        return String.format("[%s] %s - %s%n", level,  time, message);
    }
}
