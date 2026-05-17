package utils;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * DateUtil - Date formatting and conversion helpers.
 */
public final class DateUtil {

    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DB_FORMAT      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SHORT_FORMAT   = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtil() {}

    public static String toDisplay(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_FORMAT);
    }

    public static String toDbFormat(LocalDate date) {
        return date == null ? "" : date.format(DB_FORMAT);
    }

    public static LocalDate fromDbFormat(String dateStr) {
        return dateStr == null || dateStr.isBlank() ? null
               : LocalDate.parse(dateStr, DB_FORMAT);
    }

    public static LocalDate fromDisplay(String dateStr) {
        return dateStr == null || dateStr.isBlank() ? null
               : LocalDate.parse(dateStr, DISPLAY_FORMAT);
    }

    public static String monthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static String monthShort(int month) {
        return Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    public static LocalDate firstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    public static LocalDate lastDayOfMonth() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(today.lengthOfMonth());
    }
}
