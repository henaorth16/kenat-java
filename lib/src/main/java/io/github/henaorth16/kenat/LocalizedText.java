package io.github.henaorth16.kenat;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A piece of text that is available in multiple languages.
 *
 * <p>The primary languages are Amharic and English, matching the TypeScript
 * {@code LocalizedText} interface.  Additional locales can be stored in the
 * {@code extras} map.
 *
 * @param amharic Amharic text (never {@code null})
 * @param english English text (never {@code null})
 * @param extras  Optional additional locale entries keyed by locale string
 *                (e.g. {@code "om"} for Oromo).  Immutable copy is stored.
 */
public record LocalizedText(String amharic, String english, Map<String, String> extras) {

    /** Convenience constructor without extras. */
    public LocalizedText(String amharic, String english) {
        this(amharic, english, Map.of());
    }

    /** Canonical constructor — defensively copies {@code extras}. */
    public LocalizedText {
        if (amharic == null) throw new IllegalArgumentException("amharic must not be null");
        if (english == null) throw new IllegalArgumentException("english must not be null");
        extras = (extras == null) ? Map.of() : Map.copyOf(extras);
    }

    /**
     * Returns the text for the given {@link Language}.
     *
     * @param lang the requested language
     * @return the localized string
     */
    public String get(Language lang) {
        return switch (lang) {
            case AMHARIC -> amharic;
            case ENGLISH -> english;
        };
    }

    /**
     * Returns the text for an arbitrary locale key, or {@link Optional#empty()}
     * when the key is not present in {@code extras} and doesn't match a primary
     * language.
     *
     * @param locale locale identifier (e.g. {@code "amharic"}, {@code "english"}, {@code "om"})
     * @return matched text wrapped in {@link Optional}
     */
    public Optional<String> get(String locale) {
        return switch (locale.toLowerCase(Locale.ROOT)) {
            case "amharic" -> Optional.of(amharic);
            case "english" -> Optional.of(english);
            default        -> Optional.ofNullable(extras.get(locale));
        };
    }
}
