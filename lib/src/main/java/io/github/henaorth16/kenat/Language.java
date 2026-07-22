package io.github.henaorth16.kenat;

/**
 * The primary display languages supported by Kenat.
 *
 * <p>Maps to the TypeScript {@code Lang} type ({@code 'amharic' | 'english' | string}).
 * Open-ended locale keys (e.g. Oromo) can be passed as raw {@link String}
 * where APIs accept them.
 */
public enum Language {

    /** Amharic — the primary language of the Ethiopian calendar. */
    AMHARIC("amharic"),

    /** English. */
    ENGLISH("english");

    private final String key;

    Language(String key) {
        this.key = key;
    }

    /** Lower-case locale key matching the TypeScript string literal. */
    public String key() {
        return key;
    }

    /**
     * Parses a locale string into a {@code Language}, case-insensitively.
     *
     * @param value locale string (e.g. {@code "amharic"}, {@code "english"})
     * @return matching enum constant
     * @throws IllegalArgumentException if the string doesn't match any language
     */
    public static Language fromString(String value) {
        for (Language lang : values()) {
            if (lang.key.equalsIgnoreCase(value)) return lang;
        }
        throw new IllegalArgumentException("Unknown language: " + value);
    }
}
