package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidTimeException;

/**
 * Represents a time in the Ethiopian 12-hour clock system.
 * Mirrors the {@code Time} class in {@code Time.ts}.
 *
 * <p>Ethiopian time is offset from Gregorian time by 6 hours:
 * 1:00 day = 07:00 Gregorian, 1:00 night = 19:00 Gregorian.
 */
public final class EthiopianTime {

    /** Ethiopian hour (1–12). */
    private final int hour;

    /** Minute (0–59). */
    private final int minute;

    /** Period: day or night. */
    private final TimePeriod period;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates an {@code EthiopianTime} from Ethiopian hour/minute/period.
     *
     * @param hour   Ethiopian hour (1–12)
     * @param minute minute (0–59)
     * @param period {@link TimePeriod#DAY} or {@link TimePeriod#NIGHT}
     * @throws InvalidTimeException if any value is out of range
     */
    public EthiopianTime(int hour, int minute, TimePeriod period) {
        if (hour < 1 || hour > 12)
            throw new InvalidTimeException("Invalid Ethiopian hour: " + hour + ". Must be 1–12.");
        if (minute < 0 || minute > 59)
            throw new InvalidTimeException("Invalid minute: " + minute + ". Must be 0–59.");
        if (period == null)
            throw new InvalidTimeException("Period must not be null.");
        this.hour   = hour;
        this.minute = minute;
        this.period = period;
    }

    /** Creates an {@code EthiopianTime} with minute defaulting to 0. */
    public EthiopianTime(int hour, TimePeriod period) {
        this(hour, 0, period);
    }

    /**
     * Creates an {@code EthiopianTime} from a Gregorian 24-hour time.
     *
     * @param hour   Gregorian hour (0–23)
     * @param minute minute (0–59)
     * @return the corresponding Ethiopian time
     * @throws InvalidTimeException if the values are out of range
     */
    public static EthiopianTime fromGregorian(int hour, int minute) {
        if (hour < 0 || hour > 23)
            throw new InvalidTimeException("Invalid Gregorian hour: " + hour + ". Must be 0–23.");
        if (minute < 0 || minute > 59)
            throw new InvalidTimeException("Invalid minute: " + minute + ". Must be 0–59.");

        int temp = hour - 6;
        if (temp < 0) temp += 24;

        TimePeriod period = (temp < 12) ? TimePeriod.DAY : TimePeriod.NIGHT;
        int ethHour = temp % 12;
        if (ethHour == 0) ethHour = 12;
        return new EthiopianTime(ethHour, minute, period);
    }

    /**
     * Parses a time string such as {@code "6:30"}, {@code "6:30 night"},
     * {@code "፮:፴"}, or {@code "፮:፴ ማታ"}.
     *
     * @param timeString the input string
     * @return parsed {@code EthiopianTime}
     * @throws InvalidTimeException on malformed input
     */
    public static EthiopianTime fromString(String timeString) {
        if (timeString == null || timeString.isBlank())
            throw new InvalidTimeException("Input must be a non-empty string.");
        if (!timeString.contains(":"))
            throw new InvalidTimeException("Time must contain ':' separator: " + timeString);

        String[] parts = timeString.strip().split("[:\\s]+");
        if (parts.length < 2)
            throw new InvalidTimeException("Invalid time string: " + timeString);

        int hour   = parseNumber(parts[0], timeString);
        int minute = parseNumber(parts[1], timeString);

        TimePeriod period = TimePeriod.DAY;
        if (parts.length > 2) {
            String p = parts[2].toLowerCase();
            if (p.equals("night") || p.equals("ማታ")) period = TimePeriod.NIGHT;
        }
        return new EthiopianTime(hour, minute, period);
    }

    private static int parseNumber(String s, String original) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            try {
                return GeezConverter.toArabic(s);
            } catch (Exception ex) {
                throw new InvalidTimeException("Invalid number '" + s + "' in: " + original);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Conversion
    // -----------------------------------------------------------------------

    /**
     * Converts this Ethiopian time to Gregorian 24-hour format.
     *
     * @return {@code int[2]} — {@code [hour, minute]}
     */
    public int[] toGregorian() {
        int gregHour = this.hour % 12;
        gregHour += (period == TimePeriod.DAY) ? 6 : 18;
        gregHour = gregHour % 24;
        return new int[]{gregHour, this.minute};
    }

    // -----------------------------------------------------------------------
    // Arithmetic
    // -----------------------------------------------------------------------

    /**
     * Returns a new {@code EthiopianTime} with the given hours/minutes added.
     *
     * @param hours   hours to add (may be negative)
     * @param minutes minutes to add (may be negative)
     * @return new {@code EthiopianTime}
     */
    public EthiopianTime add(int hours, int minutes) {
        int[] greg = toGregorian();
        int total = greg[0] * 60 + greg[1] + hours * 60 + minutes;
        total = Math.floorMod(total, 1440);
        return EthiopianTime.fromGregorian(total / 60, total % 60);
    }

    /**
     * Returns a new {@code EthiopianTime} with the given hours/minutes subtracted.
     *
     * @param hours   hours to subtract
     * @param minutes minutes to subtract
     * @return new {@code EthiopianTime}
     */
    public EthiopianTime subtract(int hours, int minutes) {
        return add(-hours, -minutes);
    }

    /**
     * Returns the absolute difference in minutes between this and another
     * {@code EthiopianTime}, taking the shortest path around the 24h clock.
     *
     * @param other the other time
     * @return {@code int[2]} — {@code [hours, minutes]}
     */
    public int[] diff(EthiopianTime other) {
        int[] t1 = toGregorian();
        int[] t2 = other.toGregorian();
        int diff = Math.abs(t1[0] * 60 + t1[1] - (t2[0] * 60 + t2[1]));
        if (diff > 720) diff = 1440 - diff;
        return new int[]{diff / 60, diff % 60};
    }

    // -----------------------------------------------------------------------
    // Formatting
    // -----------------------------------------------------------------------

    /**
     * Formats this time as a human-readable string.
     *
     * <p>Options:
     * <ul>
     *   <li>{@code lang} — {@code "amharic"} (default) or {@code "english"}</li>
     *   <li>{@code useGeez} — use Ge'ez numerals (default {@code true})</li>
     *   <li>{@code showPeriodLabel} — append period label (default {@code true})</li>
     *   <li>{@code zeroAsDash} — show {@code _} when minute is 0 (default {@code true})</li>
     * </ul>
     *
     * @param lang            language for period label
     * @param useGeez         whether to use Ge'ez numerals
     * @param showPeriodLabel whether to append the period label
     * @param zeroAsDash      whether to replace zero minutes with "_"
     * @return formatted string, e.g. {@code "፮:፴ ጠዋት"} or {@code "06:30 day"}
     */
    public String format(String lang, boolean useGeez, boolean showPeriodLabel, boolean zeroAsDash) {
        String hourStr   = useGeez ? GeezConverter.toGeez(hour) : String.format("%02d", hour);
        String minuteStr;
        if (zeroAsDash && minute == 0) {
            minuteStr = "_";
        } else {
            minuteStr = useGeez ? GeezConverter.toGeez(minute) : String.format("%02d", minute);
        }

        String label = "";
        if (showPeriodLabel) {
            if ("english".equalsIgnoreCase(lang)) {
                label = " " + period.key();
            } else {
                label = " " + (period == TimePeriod.DAY
                        ? KenatConstants.PERIOD_LABEL_DAY
                        : KenatConstants.PERIOD_LABEL_NIGHT);
            }
        }
        return hourStr + ":" + minuteStr + label;
    }

    /** Formats with Ge'ez numerals and Amharic period label (default). */
    public String format() {
        return format("amharic", true, true, true);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public int hour()       { return hour; }
    public int minute()     { return minute; }
    public TimePeriod period() { return period; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return format("amharic", false, true, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EthiopianTime t)) return false;
        return hour == t.hour && minute == t.minute && period == t.period;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * hour + minute) + period.hashCode();
    }
}
