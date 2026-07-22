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

    /** Compact constructor — throws {@link IllegalArgumentException} on null. */
    public DateRange {
        if (start == null) throw new IllegalArgumentException("start must not be null");
        if (end   == null) throw new IllegalArgumentException("end must not be null");
    }
}
