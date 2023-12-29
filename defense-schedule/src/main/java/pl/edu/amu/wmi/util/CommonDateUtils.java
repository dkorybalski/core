package pl.edu.amu.wmi.util;


import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;

public class CommonDateUtils {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static DateTimeFormatter commonDateFormatter() {
        return dateTimeFormatter;
    }

    /**
     * Calculate defense days between selected date range. Add 1 to numOfDaysBetween to also include the last day as
     * selected.
     */
    public static List<LocalDate> getDefenseDays(LocalDate startDate, LocalDate endDate) {
        long numOfDaysBetween = Duration.ofDays(DAYS.between(startDate, endDate)).toDays();

        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween + 1)
                .mapToObj(startDate::plusDays)
                .toList();
    }

    public static String getDateStringWithTheDayOfWeek(LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.US);
        return date.format(commonDateFormatter()) + " | " + dayOfWeek;
    }

    public static LocalDate parseDateStringWithTheDayOfWeekToLocalDate(String date) {
        String dateWithoutTheDayOfTheWeek = date.split(" ")[0];
        return LocalDate.parse(dateWithoutTheDayOfTheWeek, commonDateFormatter());
    }

}
