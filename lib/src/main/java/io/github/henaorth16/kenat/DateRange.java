package io.github.henaorth16.kenat;

import java.util.Objects;

/**
 * An inclusive range of Ethiopian calendar dates.
 *
 * <p>Maps to the TypeScript {@code DateRange} interface.  Used by fasting period
 * calculations and other date-span queries.
 *
 * @param start First day of the range (inclusive)
 * @param end   Last day of the range (inclusive)
 */
public record DateRange(EthiopianDate start, EthiopianDate end) {

    /** Compact constructor — null guard. */
    public DateRange {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end,   "end must not be null");
    }
}
