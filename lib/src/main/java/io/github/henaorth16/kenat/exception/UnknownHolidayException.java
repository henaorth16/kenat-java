package io.github.henaorth16.kenat.exception;

/**
 * Thrown when an unknown holiday key is used in a lookup that requires a
 * recognised key (e.g. {@code getMovableHoliday}).
 *
 * <p>Mirrors the TypeScript {@code UnknownHolidayError}.
 */
public class UnknownHolidayException extends KenatException {

    private final String holidayKey;

    /**
     * @param holidayKey the unrecognised key
     */
    public UnknownHolidayException(String holidayKey) {
        super(String.format("Unknown movable holiday key: \"%s\"", holidayKey));
        this.holidayKey = holidayKey;
    }

    /** The key that was not found. */
    public String getHolidayKey() {
        return holidayKey;
    }
}
