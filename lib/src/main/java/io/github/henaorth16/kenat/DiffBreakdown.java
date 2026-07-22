package io.github.henaorth16.kenat;

import java.util.OptionalInt;

/**
 * The result of decomposing a date difference into years, months, and/or days.
 *
 * <p>Maps to the TypeScript {@code DiffBreakdown} interface.  The optional
 * component fields are present only when the corresponding {@link DiffUnit}
 * was requested.
 *
 * @param sign      Direction: {@code 1} if {@code from} is before {@code to},
 *                  {@code -1} otherwise
 * @param totalDays Absolute number of days between the two dates
 * @param years     Year component (null / absent when not requested)
 * @param months    Month component (null / absent when not requested)
 * @param days      Remaining day component (null / absent when not requested)
 */
public record DiffBreakdown(
        int sign,
        int totalDays,
        Integer years,
        Integer months,
        Integer days
) {

    /** Compact constructor — validates sign. */
    public DiffBreakdown {
        if (sign != 1 && sign != -1) {
            throw new IllegalArgumentException("sign must be 1 or -1, got: " + sign);
        }
        if (totalDays < 0) {
            throw new IllegalArgumentException("totalDays must be non-negative");
        }
    }

    /** Returns the years component wrapped in {@link OptionalInt}. */
    public OptionalInt yearsOpt() {
        return years == null ? OptionalInt.empty() : OptionalInt.of(years);
    }

    /** Returns the months component wrapped in {@link OptionalInt}. */
    public OptionalInt monthsOpt() {
        return months == null ? OptionalInt.empty() : OptionalInt.of(months);
    }

    /** Returns the remaining days component wrapped in {@link OptionalInt}. */
    public OptionalInt daysOpt() {
        return days == null ? OptionalInt.empty() : OptionalInt.of(days);
    }
}
