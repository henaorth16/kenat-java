package io.github.henaorth16.kenat.exception;

/**
 * Thrown when an Ethiopian date value is numerically invalid (e.g. month 14,
 * day 0, or a Pagume day beyond the allowed 5 or 6).
 *
 * <p>Mirrors the TypeScript {@code InvalidEthiopianDateError}.
 */
public class InvalidEthiopianDateException extends KenatException {

    private final Object year;
    private final Object month;
    private final Object day;

    /**
     * @param year  the invalid year value (stored for diagnostics)
     * @param month the invalid month value
     * @param day   the invalid day value
     */
    public InvalidEthiopianDateException(Object year, Object month, Object day) {
        super(String.format("Invalid Ethiopian date: %s/%s/%s", year, month, day));
        this.year  = year;
        this.month = month;
        this.day   = day;
    }

    /** The year component that was rejected. */
    public Object getYear()  { return year;  }

    /** The month component that was rejected. */
    public Object getMonth() { return month; }

    /** The day component that was rejected. */
    public Object getDay()   { return day;   }

    /**
     * Convenience factory for the common {@code int} overload.
     *
     * @param year  year component
     * @param month month component
     * @param day   day component
     * @return a new exception describing the invalid date
     */
    public static InvalidEthiopianDateException of(int year, int month, int day) {
        return new InvalidEthiopianDateException(year, month, day);
    }
}
