package io.github.henaorth16.kenat;

import java.util.List;
import java.util.Optional;

/**
 * A single holiday occurrence for a specific Ethiopian year.
 *
 * <p>Maps to the TypeScript {@code Holiday} interface.  Fixed (non-movable)
 * holidays always have the same Ethiopian date each year; movable holidays are
 * computed from the Bahire Hasab algorithm.
 *
 * @param key       Unique holiday identifier (e.g. {@code "fasika"})
 * @param tags      Classification tags such as {@code "public"}, {@code "christian"}, {@code "muslim"}
 * @param movable   {@code true} if this is a Bahire-Hasab-computed feast day
 * @param name      Localized display name, or {@code null} if not resolved yet
 * @param description Localized description, or {@code null} if not resolved yet
 * @param ethiopian Ethiopian date of the holiday
 * @param gregorian Gregorian date, present for most holidays; may be absent for
 *                  purely liturgical internal keys
 */
public record Holiday(
        String key,
        List<String> tags,
        boolean movable,
        String name,
        String description,
        EthiopianDate ethiopian,
        GregorianDate gregorian
) {

    /** Canonical constructor — makes the tags list immutable. */
    public Holiday {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    /** Convenience accessor wrapping the nullable gregorian field. */
    public Optional<GregorianDate> gregorianDate() {
        return Optional.ofNullable(gregorian);
    }

    /**
     * Returns {@code true} if this holiday carries the given classification tag.
     *
     * @param tag tag to test (case-sensitive)
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}
