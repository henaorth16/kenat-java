package io.github.henaorth16.kenat;

import java.time.LocalDate;
import java.util.List;

/**
 * Date and time formatting utilities.
 * Mirrors {@code formatting.ts} from the TypeScript implementation.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class Formatting {

    private Formatting() {}

    // -----------------------------------------------------------------------
    // Ethiopian date formatting
    // -----------------------------------------------------------------------

    /**
     * Formats an Ethiopian date as {@code "<MonthName> <day> <year>"}.
     * Example: {@code "መስከረም 10 2016"} (Amharic) or {@code "Meskerem 10 2016"} (English).
     *
     * @param date Ethiopian date
     * @param lang {@code "amharic"} or {@code "english"}
     * @return formatted string
     */
    public static String formatStandard(EthiopianDate date, String lang) {
        List<String> names = getMonthNames(lang);
        String monthName = names.get(date.month() - 1);
        return monthName + " " + date.day() + " " + date.year();
    }

    /** Formats using Amharic month name (default). */
    public static String formatStandard(EthiopianDate date) {
        return formatStandard(date, "amharic");
    }

    /**
     * Formats an Ethiopian date using Ge'ez numerals with the Amharic month name.
     * Example: {@code "መስከረም ፲ ፳፻፲፮"}.
     *
     * @param date Ethiopian date
     * @return formatted string
     */
    public static String formatInGeezAmharic(EthiopianDate date) {
        String monthName = KenatConstants.MONTH_NAMES_AMHARIC.get(date.month() - 1);
        return monthName + " " + GeezConverter.toGeez(date.day()) + " " + GeezConverter.toGeez(date.year());
    }

    /**
     * Formats an Ethiopian date with the weekday name prepended.
     * Example: {@code "ማክሰኞ, መስከረም 1 2016"}.
     *
     * @param date    Ethiopian date
     * @param lang    language for names
     * @param useGeez whether to display day and year in Ge'ez numerals
     * @return formatted string
     */
    public static String formatWithWeekday(EthiopianDate date, String lang, boolean useGeez) {
        int weekdayIndex   = EthiopianCalendarUtils.getWeekday(date);
        List<String> wdays = getDaysOfWeek(lang);
        List<String> mons  = getMonthNames(lang);
        String weekday     = wdays.get(weekdayIndex);
        String monthName   = mons.get(date.month() - 1);
        String day  = useGeez ? GeezConverter.toGeez(date.day())  : String.valueOf(date.day());
        String year = useGeez ? GeezConverter.toGeez(date.year()) : String.valueOf(date.year());
        return weekday + ", " + monthName + " " + day + " " + year;
    }

    /** Formats with weekday, default Amharic, no Ge'ez numerals. */
    public static String formatWithWeekday(EthiopianDate date) {
        return formatWithWeekday(date, "amharic", false);
    }

    /**
     * Formats an Ethiopian date with time appended.
     * Example: {@code "መስከረም 10 2016 08:30 ጠዋት"}.
     *
     * @param date Ethiopian date
     * @param time Ethiopian time
     * @param lang language for names
     * @return formatted string
     */
    public static String formatWithTime(EthiopianDate date, EthiopianTime time, String lang) {
        String base = formatStandard(date, lang);
        String timeStr = time.format(lang, false, true, false);
        return base + " " + timeStr;
    }

    /**
     * Returns the Ethiopian date in short {@code "yyyy/mm/dd"} format.
     *
     * @param date Ethiopian date
     * @return e.g. {@code "2017/10/25"}
     */
    public static String formatShort(EthiopianDate date) {
        return String.format("%d/%02d/%02d", date.year(), date.month(), date.day());
    }

    /**
     * Returns an ISO-like string: {@code "YYYY-MM-DD"} or
     * {@code "YYYY-MM-DDTHH:mm"} (Ethiopian time, non-standard {@code +12h}
     * suffix for night period).
     *
     * @param date Ethiopian date
     * @param time optional time ({@code null} for date-only)
     * @return ISO-like string
     */
    public static String toISODateString(EthiopianDate date, EthiopianTime time) {
        String d = String.format("%d-%02d-%02d", date.year(), date.month(), date.day());
        if (time == null) return d;
        String suffix = (time.period() == TimePeriod.NIGHT) ? "+12h" : "";
        return String.format("%sT%02d:%02d%s", d, time.hour(), time.minute(), suffix);
    }

    /** Returns date-only ISO-like string. */
    public static String toISODateString(EthiopianDate date) {
        return toISODateString(date, null);
    }

    // -----------------------------------------------------------------------
    // Gregorian date formatting
    // -----------------------------------------------------------------------

    /**
     * Formats a Gregorian date as {@code "<MonthName> <day>, <year>"}.
     * Example: {@code "September 11, 2023"}.
     *
     * @param date Gregorian date
     * @return formatted string
     */
    public static String formatGregorianStandard(GregorianDate date) {
        String monthName = KenatConstants.MONTH_NAMES_GREGORIAN.get(date.month() - 1);
        return monthName + " " + date.day() + ", " + date.year();
    }

    /**
     * Formats a Gregorian date with the weekday name prepended.
     * Example: {@code "Monday, September 11, 2023"}.
     *
     * @param date Gregorian date
     * @param lang language for weekday name
     * @return formatted string
     */
    public static String formatGregorianWithWeekday(GregorianDate date, String lang) {
        LocalDate ld = LocalDate.of(date.year(), date.month(), date.day());
        int javaOrdinal = ld.getDayOfWeek().getValue(); // Mon=1 … Sun=7
        int weekdayIndex = javaOrdinal % 7;             // Sun=0, Mon=1 … Sat=6
        List<String> wdays = getDaysOfWeek(lang);
        return wdays.get(weekdayIndex) + ", " + formatGregorianStandard(date);
    }

    /** Formats with weekday in English. */
    public static String formatGregorianWithWeekday(GregorianDate date) {
        return formatGregorianWithWeekday(date, "english");
    }

    /**
     * Formats a Gregorian date with Ethiopian time appended.
     *
     * @param date Gregorian date
     * @param time Ethiopian time
     * @param lang language for time period label
     * @return formatted string
     */
    public static String formatGregorianWithTime(GregorianDate date, EthiopianTime time, String lang) {
        return formatGregorianStandard(date) + " " + time.format(lang, false, true, false);
    }

    /**
     * Returns a standard ISO 8601 date-time string for a Gregorian date,
     * converting the Ethiopian time to Gregorian 24h format.
     *
     * @param date Gregorian date
     * @param time Ethiopian time (or {@code null} for date-only)
     * @return valid ISO 8601 string
     */
    public static String toGregorianISODateString(GregorianDate date, EthiopianTime time) {
        String d = String.format("%d-%02d-%02d", date.year(), date.month(), date.day());
        if (time == null) return d;
        int[] greg = time.toGregorian();
        return String.format("%sT%02d:%02d", d, greg[0], greg[1]);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List<String> getMonthNames(String lang) {
        if ("english".equalsIgnoreCase(lang)) return KenatConstants.MONTH_NAMES_ENGLISH;
        return KenatConstants.MONTH_NAMES_AMHARIC;
    }

    private static List<String> getDaysOfWeek(String lang) {
        if ("english".equalsIgnoreCase(lang)) return KenatConstants.DAYS_OF_WEEK_ENGLISH;
        return KenatConstants.DAYS_OF_WEEK_AMHARIC;
    }
}
