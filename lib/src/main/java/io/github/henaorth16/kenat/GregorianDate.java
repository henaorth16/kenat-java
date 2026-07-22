package io.github.henaorth16.kenat;

import java.time.LocalDate;

/**
 * Immutable value type representing a date in the Gregorian calendar.
 *
 * <p>Kenat keeps Ethiopian and Gregorian dates as separate, explicit types so
 * there is never ambiguity about which calendar system a value belongs to.
 *
 * @param year  Gregorian year
 * @param month Gregorian month, 1–12
 * @param day   Day of month, 1–31 (depending on month)
 */
public record GregorianDate(int year, int month, int day) {

    /**
     * Compact constructor — validates obvious out-of-range values.
     *
     * @throws io.github.henaorth16.kenat.exception.InvalidGregorianDateException
     *         if any component is out of range
     */
    public GregorianDate {
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            throw new io.github.henaorth16.kenat.exception.InvalidGregorianDateException(year, month, day);
        }
    }

    /**
     * Converts this record to a {@link LocalDate} for use with {@code java.time}.
     *
     * @return equivalent {@link LocalDate}
     * @throws java.time.DateTimeException if the combination is not a valid calendar date
     */
    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }

    /**
     * Creates a {@code GregorianDate} from a {@link LocalDate}.
     *
     * @param date the source date, must not be {@code null}
     * @return equivalent {@code GregorianDate}
     */
    public static GregorianDate from(LocalDate date) {
        return new GregorianDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Returns a human-readable representation in {@code yyyy-MM-dd} (ISO) form.
     */
    @Override
    public String toString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
