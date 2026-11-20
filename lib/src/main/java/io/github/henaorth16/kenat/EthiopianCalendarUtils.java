package io.github.henaorth16.kenat;

import java.time.LocalDate;

/**
 * Utility methods for Ethiopian and Gregorian calendar arithmetic.
 * Mirrors {@code utils.ts} from the TypeScript implementation.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class EthiopianCalendarUtils {

    private EthiopianCalendarUtils() {}

    // -----------------------------------------------------------------------
    // Leap-year checks
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if the given Gregorian year is a leap year.
     * Leap years occur every 4 years, except centuries not divisible by 400.
     *
     * @param year Gregorian year, e.g. 2024
     * @return {@code true} if leap year
     */
    public static boolean isGregorianLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Returns {@code true} if the given Ethiopian year is a leap year.
     * Ethiopian leap years occur when {@code year % 4 == 3}.
     *
     * @param year Ethiopian year, e.g. 2015
     * @return {@code true} if leap year
     */
    public static boolean isEthiopianLeapYear(int year) {
        return year % 4 == 3;
    }

    // -----------------------------------------------------------------------
    // Days in month
    // -----------------------------------------------------------------------

    /**
     * Returns the number of days in the given Ethiopian month/year.
     * Months 1–12 always have 30 days; month 13 (Pagume) has 5 or 6.
     *
     * @param year  Ethiopian year
     * @param month Ethiopian month (1–13)
     * @return number of days in the month
     */
    public static int getEthiopianDaysInMonth(int year, int month) {
        if (month == 13) {
            return isEthiopianLeapYear(year) ? 6 : 5;
        }
        return 30;
    }

    // -----------------------------------------------------------------------
    // Day of year (Gregorian)
    // -----------------------------------------------------------------------

    /**
     * Calculates the 1-based day-of-year for a Gregorian date.
     *
     * @param year  Gregorian year
     * @param month Gregorian month (1–12)
     * @param day   day of month
     * @return day of year (1-based)
     */
    public static int dayOfYear(int year, int month, int day) {
        int[] lengths = gregorianMonthLengths(year);
        int doy = 0;
        for (int i = 0; i < month - 1; i++) {
            doy += lengths[i];
        }
        return doy + day;
    }

    /**
     * Converts a Gregorian day-of-year back to a month and day.
     *
     * @param year      Gregorian year
     * @param dayOfYear 1-based day of year
     * @return {@code int[2]} where {@code [0]} = month and {@code [1]} = day
     */
    public static int[] monthDayFromDayOfYear(int year, int dayOfYear) {
        int[] lengths = gregorianMonthLengths(year);
        int month = 1;
        while (dayOfYear > lengths[month - 1]) {
            dayOfYear -= lengths[month - 1];
            month++;
        }
        return new int[]{month, dayOfYear};
    }

    private static int[] gregorianMonthLengths(int year) {
        return new int[]{31, isGregorianLeapYear(year) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    }

    // -----------------------------------------------------------------------
    // Ethiopian New Year helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the Gregorian date of the Ethiopian New Year (Enkutatash) that
     * falls within the given Gregorian year.
     *
     * <p>Enkutatash is always in September of {@code gYear} and starts
     * Ethiopian year {@code gYear - 7}; see
     * {@link #getGregorianDateOfEthiopianNewYear} for how the day is found.
     *
     * <p><strong>Deviation from the TypeScript library:</strong>
     * {@code getEthiopianNewYearForGregorian} in {@code utils.ts} tests the
     * <em>preceding</em> Gregorian year ({@code gYear - 1}) instead of the
     * following one, which returns the wrong day for roughly half of all years
     * (e.g. it gives 11 September 2023 where the correct answer, and the one
     * {@link #getGregorianDateOfEthiopianNewYear} produces, is 12 September).
     * The function is unused inside the TypeScript library, so the bug is not
     * observable there; here it is corrected so the two helpers agree.
     *
     * @param gYear Gregorian year
     * @return {@code int[3]}: {@code [gregorianYear, month, day]}
     */
    public static int[] getEthiopianNewYearForGregorian(int gYear) {
        return getGregorianDateOfEthiopianNewYear(gYear - 7);
    }

    /**
     * Returns the Gregorian date of the Ethiopian New Year (Enkutatash) for the
     * given Ethiopian year.
     *
     * <p>Counted in days from Meskerem 1, 1964 (12 September 1971). Ethiopian
     * leap years ({@code year % 4 == 3}) have no century exception, so the
     * date drifts relative to the Gregorian calendar across 1900, 2100, etc.
     *
     * @param ethiopianYear Ethiopian calendar year
     * @return {@code int[3]}: {@code [gregorianYear, month, day]}
     */
    public static int[] getGregorianDateOfEthiopianNewYear(int ethiopianYear) {
        LocalDate date = ethiopianNewYearDate(ethiopianYear);
        return new int[]{date.getYear(), date.getMonthValue(), date.getDayOfMonth()};
    }

    /** Meskerem 1 of {@code ethiopianYear} as a {@link LocalDate}. */
    static LocalDate ethiopianNewYearDate(int ethiopianYear) {
        long days = 365L * (ethiopianYear - 1964)
                + Math.floorDiv(ethiopianYear, 4) - Math.floorDiv(1964, 4);
        return LocalDate.of(1971, 9, 12).plusDays(days);
    }

    // -----------------------------------------------------------------------
    // Weekday
    // -----------------------------------------------------------------------

    /**
     * Returns the ISO day-of-week index (0 = Sunday … 6 = Saturday) for a
     * given Ethiopian date.  Converts to Gregorian first, then uses
     * {@link LocalDate#getDayOfWeek()}.
     *
     * @param date Ethiopian date
     * @return 0 (Sunday) … 6 (Saturday)
     */
    public static int getWeekday(EthiopianDate date) {
        GregorianDate g = Conversions.toGC(date.year(), date.month(), date.day());
        LocalDate ld = LocalDate.of(g.year(), g.month(), g.day());
        // Java DayOfWeek: MONDAY=1 … SUNDAY=7.  Convert to 0=Sun … 6=Sat.
        int javaOrdinal = ld.getDayOfWeek().getValue(); // 1=Mon … 7=Sun
        return javaOrdinal % 7; // Mon→1, Tue→2, …, Sat→6, Sun→0
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if the given Ethiopian year/month/day combination is a
     * valid calendar date (including the leap-year check for Pagume day 6).
     *
     * @param year  Ethiopian year
     * @param month Ethiopian month (1–13)
     * @param day   day of month
     * @return {@code true} if the date is valid
     */
    public static boolean isValidEthiopianDate(int year, int month, int day) {
        if (month < 1 || month > 13) return false;
        if (day < 1 || day > getEthiopianDaysInMonth(year, month)) return false;
        return true;
    }
}
