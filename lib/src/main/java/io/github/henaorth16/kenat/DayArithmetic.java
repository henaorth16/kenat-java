package io.github.henaorth16.kenat;

import java.util.EnumSet;
import java.util.Set;
import java.util.Collection;

import static io.github.henaorth16.kenat.EthiopianCalendarUtils.*;

/**
 * Date arithmetic on Ethiopian calendar dates.
 * Mirrors {@code dayArithmetic.ts} from the TypeScript implementation.
 *
 * <p>All methods are static and return new {@link EthiopianDate} instances;
 * inputs are never mutated.
 */
public final class DayArithmetic {

    private DayArithmetic() {}

    // -----------------------------------------------------------------------
    // Add
    // -----------------------------------------------------------------------

    /**
     * Adds a number of days to an Ethiopian date (negative values move backwards).
     *
     * @param date Ethiopian date
     * @param days number of days to add
     * @return new {@link EthiopianDate} with the result
     */
    public static EthiopianDate addDays(EthiopianDate date, int days) {
        int year  = date.year();
        int month = date.month();
        int day   = date.day() + days;

        // forward
        while (day > getEthiopianDaysInMonth(year, month)) {
            day -= getEthiopianDaysInMonth(year, month);
            month++;
            if (month > 13) { month = 1; year++; }
        }
        // backward
        while (day <= 0) {
            month--;
            if (month < 1) { month = 13; year--; }
            day += getEthiopianDaysInMonth(year, month);
        }
        return new EthiopianDate(year, month, day);
    }

    /**
     * Adds a number of months to an Ethiopian date.
     * If the resulting month has fewer days than the current day, the day is
     * clamped to the last valid day of that month.
     *
     * @param date   Ethiopian date
     * @param months number of months to add (may be negative)
     * @return new {@link EthiopianDate} with the result
     */
    public static EthiopianDate addMonths(EthiopianDate date, int months) {
        int totalMonths = date.month() + months;
        int year  = date.year() + Math.floorDiv(totalMonths - 1, 13);
        int month = Math.floorMod(totalMonths - 1, 13) + 1;
        int day   = Math.min(date.day(), getEthiopianDaysInMonth(year, month));
        return new EthiopianDate(year, month, day);
    }

    /**
     * Adds a number of years to an Ethiopian date.
     * Clamps Pagume 6 to Pagume 5 if the resulting year is not an Ethiopian leap year.
     *
     * @param date  Ethiopian date
     * @param years number of years to add (may be negative)
     * @return new {@link EthiopianDate} with the result
     */
    public static EthiopianDate addYears(EthiopianDate date, int years) {
        int year = date.year() + years;
        int day  = date.day();
        if (date.month() == 13 && day == 6 && !isEthiopianLeapYear(year)) {
            day = 5;
        }
        return new EthiopianDate(year, date.month(), day);
    }

    // -----------------------------------------------------------------------
    // Diff
    // -----------------------------------------------------------------------

    /**
     * Calculates the signed difference in days between two Ethiopian dates.
     * Positive when {@code a} is after {@code b}.
     *
     * @param a first date
     * @param b second date
     * @return {@code a - b} in days
     */
    public static int diffInDays(EthiopianDate a, EthiopianDate b) {
        return totalDays(a) - totalDays(b);
    }

    /**
     * Calculates the signed difference in months between two Ethiopian dates.
     * Adjusts by -1 if {@code a}'s day is less than {@code b}'s day
     * (partial month not yet completed).
     *
     * @param a first date
     * @param b second date
     * @return difference in months
     */
    public static int diffInMonths(EthiopianDate a, EthiopianDate b) {
        int totalA = a.year() * 13 + (a.month() - 1);
        int totalB = b.year() * 13 + (b.month() - 1);
        int diff   = totalA - totalB;
        if (a.day() < b.day()) diff--;
        return diff;
    }

    /**
     * Calculates the signed difference in years between two Ethiopian dates.
     * Adjusts by -1 if the anniversary month/day has not yet been reached.
     *
     * @param a first date
     * @param b second date
     * @return difference in years
     */
    public static int diffInYears(EthiopianDate a, EthiopianDate b) {
        boolean isAfter = (a.year() > b.year())
                || (a.year() == b.year() && a.month() > b.month())
                || (a.year() == b.year() && a.month() == b.month() && a.day() >= b.day());

        EthiopianDate later   = isAfter ? a : b;
        EthiopianDate earlier = isAfter ? b : a;

        int diff = later.year() - earlier.year();
        if (later.month() < earlier.month()
                || (later.month() == earlier.month() && later.day() < earlier.day())) {
            diff--;
        }
        return isAfter ? diff : -diff;
    }

    /**
     * Calculates a human-friendly breakdown between two Ethiopian dates.
     * Iteratively accumulates years, then months, then days.
     *
     * @param a       first date
     * @param b       second date
     * @param units   which units to include; pass {@code null} for all three
     * @return a {@link DiffBreakdown} record with sign, totalDays, and optional years/months/days
     */
    public static DiffBreakdown diffBreakdown(EthiopianDate a, EthiopianDate b, Set<DiffUnit> units) {
        if (units == null) units = EnumSet.allOf(DiffUnit.class);

        int totalDaysDiff = diffInDays(a, b);
        int sign          = totalDaysDiff >= 0 ? 1 : -1;
        EthiopianDate later   = sign >= 0 ? a : b;
        EthiopianDate earlier = sign >= 0 ? b : a;

        EthiopianDate cursor = earlier;
        Integer years  = null;
        Integer months = null;
        Integer days   = null;

        if (units.contains(DiffUnit.YEARS)) {
            int y = 0;
            while (true) {
                EthiopianDate next = addYears(cursor, 1);
                if (diffInDays(later, next) >= 0) { y++; cursor = next; }
                else break;
            }
            years = y;
        }

        if (units.contains(DiffUnit.MONTHS)) {
            int m = 0;
            while (true) {
                EthiopianDate next = addMonths(cursor, 1);
                if (diffInDays(later, next) >= 0) { m++; cursor = next; }
                else break;
            }
            months = m;
        }

        if (units.contains(DiffUnit.DAYS)) {
            days = Math.abs(diffInDays(later, cursor));
        }

        return new DiffBreakdown(sign, Math.abs(totalDaysDiff), years, months, days);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Absolute day count from year 1, used for diffInDays. */
    private static int totalDays(EthiopianDate eth) {
        int days = 0;
        for (int y = 1; y < eth.year(); y++) {
            days += isEthiopianLeapYear(y) ? 366 : 365;
        }
        for (int m = 1; m < eth.month(); m++) {
            days += getEthiopianDaysInMonth(eth.year(), m);
        }
        return days + eth.day();
    }
}
