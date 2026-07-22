package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidEthiopianDateException;
import io.github.henaorth16.kenat.exception.InvalidGregorianDateException;

import java.time.LocalDate;

import static io.github.henaorth16.kenat.EthiopianCalendarUtils.*;

/**
 * Ethiopian ↔ Gregorian date conversion.
 * Mirrors {@code conversions.ts} from the TypeScript implementation.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class Conversions {

    private Conversions() {}

    // -----------------------------------------------------------------------
    // Ethiopian → Gregorian
    // -----------------------------------------------------------------------

    /**
     * Converts an Ethiopian date to the corresponding Gregorian date.
     *
     * @param ethYear  Ethiopian year
     * @param ethMonth Ethiopian month (1–13)
     * @param ethDay   Ethiopian day of month
     * @return the equivalent {@link GregorianDate}
     * @throws InvalidEthiopianDateException if the date is out of range
     */
    public static GregorianDate toGC(int ethYear, int ethMonth, int ethDay) {
        if (ethMonth < 1 || ethMonth > 13) {
            throw new InvalidEthiopianDateException(ethYear, ethMonth, ethDay);
        }
        int maxDay = (ethMonth == 13) ? (isEthiopianLeapYear(ethYear) ? 6 : 5) : 30;
        if (ethDay < 1 || ethDay > maxDay) {
            throw new InvalidEthiopianDateException(ethYear, ethMonth, ethDay);
        }

        int[] newYear = getGregorianDateOfEthiopianNewYear(ethYear);
        int daysSinceNewYear = (ethMonth - 1) * 30 + ethDay - 1;
        int newYearDOY = dayOfYear(newYear[0], newYear[1], newYear[2]);
        int gregorianDOY = newYearDOY + daysSinceNewYear;
        int gregorianYear = newYear[0];

        int yearLength = isGregorianLeapYear(gregorianYear) ? 366 : 365;
        if (gregorianDOY > yearLength) {
            gregorianDOY -= yearLength;
            gregorianYear += 1;
        }

        int[] md = monthDayFromDayOfYear(gregorianYear, gregorianDOY);
        return new GregorianDate(gregorianYear, md[0], md[1]);
    }

    /**
     * Converts an Ethiopian date record to the corresponding Gregorian date.
     *
     * @param date Ethiopian date
     * @return the equivalent {@link GregorianDate}
     */
    public static GregorianDate toGC(EthiopianDate date) {
        return toGC(date.year(), date.month(), date.day());
    }

    /**
     * Converts an Ethiopian date to a {@link LocalDate}.
     *
     * @param ethYear  Ethiopian year
     * @param ethMonth Ethiopian month
     * @param ethDay   Ethiopian day
     * @return the equivalent {@link LocalDate}
     */
    public static LocalDate toGCDate(int ethYear, int ethMonth, int ethDay) {
        GregorianDate g = toGC(ethYear, ethMonth, ethDay);
        return LocalDate.of(g.year(), g.month(), g.day());
    }

    // -----------------------------------------------------------------------
    // Gregorian → Ethiopian
    // -----------------------------------------------------------------------

    /**
     * Converts a Gregorian date to the corresponding Ethiopian date.
     *
     * @param gYear  Gregorian year
     * @param gMonth Gregorian month (1–12)
     * @param gDay   Gregorian day of month
     * @return the equivalent {@link EthiopianDate}
     * @throws InvalidGregorianDateException if the date is invalid or outside 1900–2100
     */
    public static EthiopianDate toEC(int gYear, int gMonth, int gDay) {
        // Validate by constructing a LocalDate (throws DateTimeException for invalid)
        LocalDate inputDate;
        try {
            inputDate = LocalDate.of(gYear, gMonth, gDay);
        } catch (Exception e) {
            throw new InvalidGregorianDateException(gYear, gMonth, gDay);
        }

        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.of(2100, 12, 31);
        if (inputDate.isBefore(minDate) || inputDate.isAfter(maxDate)) {
            throw new InvalidGregorianDateException(gYear, gMonth, gDay);
        }

        // Epoch: 1971-09-12 (Ethiopian New Year 1964/1/1)
        LocalDate baseDate = LocalDate.of(1971, 9, 12);
        long diff = inputDate.toEpochDay() - baseDate.toEpochDay();

        long oneYear     = 365L;
        long fourYears   = 1461L; // 365*4+1

        long fourYearCycles  = Math.floorDiv(diff, fourYears);
        long remaining       = diff - fourYearCycles * fourYears;
        long remainingYears  = Math.floorDiv(remaining, oneYear);
        if (remainingYears == 4) remainingYears = 3;

        long afterYears      = remaining - remainingYears * oneYear;
        long remainingMonths = Math.floorDiv(afterYears, 30L);
        long remainingDays   = afterYears - remainingMonths * 30L;

        int ethYear = (int) (1964 + fourYearCycles * 4 + remainingYears);
        int month   = (int) (remainingMonths + 1);
        int day     = (int) (remainingDays + 1);

        return new EthiopianDate(ethYear, month, day);
    }

    /**
     * Converts a {@link LocalDate} to the corresponding Ethiopian date.
     *
     * @param date Gregorian local date
     * @return the equivalent {@link EthiopianDate}
     */
    public static EthiopianDate toEC(LocalDate date) {
        return toEC(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}
