package io.github.henaorth16.kenat.exception;

/**
 * Thrown when a Gregorian date value is numerically invalid or outside the
 * supported conversion range (1900–2100).
 *
 * <p>Mirrors the TypeScript {@code InvalidGregorianDateError}.
 */
public class InvalidGregorianDateException extends KenatException {

    private final Object year;
    private final Object month;
    private final Object day;

    /**
     * @param year  the invalid year value (stored for diagnostics)
     * @param month the invalid month value
     * @param day   the invalid day value
     */
    public InvalidGregorianDateException(Object year, Object month, Object day) {
        super(String.format("Invalid Gregorian date: %s/%s/%s", year, month, day));
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
}
