package testBase.listners;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import testBase.documetation.DateFormat;

public class DateTimeUtils {
	protected static String getDuration(String startTime2, String endTime2) throws ParseException {
		final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendValue(ChronoField.DAY_OF_MONTH, 2)
				.appendLiteral('.').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('.')
				.appendValueReduced(ChronoField.YEAR, 2, 2, 2000).appendLiteral(' ')
				.appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
				.appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		final LocalDateTime start = LocalDateTime.parse(dateFormatChange(startTime2,DateFormat.dateFormatAPIDuration), formatter);
		final LocalDateTime stop = LocalDateTime.parse(dateFormatChange(endTime2,DateFormat.dateFormatAPIDuration), formatter);

		final Duration duration = Duration.between(start, stop);

		long hours = duration.toHours();
		long minutes = duration.toMinutesPart();
		long seconds = duration.toSecondsPart();
		long milliseconds = duration.toMillisPart();
		return hours + ":" + minutes + ":" + seconds + "." + milliseconds;
	}

	protected static String dateWithTime() {
		return new SimpleDateFormat(DateFormat.dateFormat).format(new Date());
	}

	protected static String dateFormatChange(String inputDate, String format) {

		// Define DateTimeFormatter for source format
		DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern(DateFormat.dateFormat);

		// Parse input string to LocalDateTime using source formatter
		LocalDateTime dateTime = LocalDateTime.parse(inputDate, sourceFormatter);

		// Define DateTimeFormatter for target format
		DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern(format);

		// Format LocalDateTime to target format
		return dateTime.format(targetFormatter);
	}
}
