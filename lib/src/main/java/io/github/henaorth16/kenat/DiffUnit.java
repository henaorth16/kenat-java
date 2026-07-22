package io.github.henaorth16.kenat;

/**
 * Units for date difference calculations.
 *
 * <p>Maps to the TypeScript {@code DiffUnit} type ({@code 'years' | 'months' | 'days'}).
 */
public enum DiffUnit {

    /** Whole Ethiopian years. */
    YEARS("years"),

    /** Whole Ethiopian months. */
    MONTHS("months"),

    /** Individual days. */
    DAYS("days");

    private final String key;

    DiffUnit(String key) {
        this.key = key;
    }

    /** Lower-case key matching the TypeScript string literal. */
    public String key() {
        return key;
    }

    /**
     * Parses a unit string into a {@code DiffUnit}, case-insensitively.
     *
     * @param value unit string ({@code "years"}, {@code "months"}, or {@code "days"})
     * @return matching enum constant
     * @throws IllegalArgumentException if the value is unrecognised
     */
    public static DiffUnit fromString(String value) {
        for (DiffUnit u : values()) {
            if (u.key.equalsIgnoreCase(value)) return u;
        }
        throw new IllegalArgumentException("Unknown diff unit: " + value);
    }
}
