package io.github.henaorth16.kenat;

/**
 * The two periods of the Ethiopian 12-hour clock.
 *
 * <p>Maps to the TypeScript {@code TimePeriod} type ({@code 'day' | 'night'}).
 * In the Ethiopian system, "day" starts at 6 AM Gregorian (Ethiopian hour 12
 * daytime) and "night" starts at 6 PM Gregorian (Ethiopian hour 12 night-time).
 */
public enum TimePeriod {

    /** Daytime period (Ethiopian hours 1–12 covering 6 AM–6 PM Gregorian). */
    DAY("day"),

    /** Night-time period (Ethiopian hours 1–12 covering 6 PM–6 AM Gregorian). */
    NIGHT("night");

    private final String key;

    TimePeriod(String key) {
        this.key = key;
    }

    /** Lower-case key matching the TypeScript string literal. */
    public String key() {
        return key;
    }

    /**
     * Parses a period string into a {@code TimePeriod}, case-insensitively.
     *
     * @param value period string ({@code "day"} or {@code "night"})
     * @return matching enum constant
     * @throws IllegalArgumentException if the value is unrecognised
     */
    public static TimePeriod fromString(String value) {
        for (TimePeriod p : values()) {
            if (p.key.equalsIgnoreCase(value)) return p;
        }
        throw new IllegalArgumentException("Unknown time period: " + value);
    }
}
