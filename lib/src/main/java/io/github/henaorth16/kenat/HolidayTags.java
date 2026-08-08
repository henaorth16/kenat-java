package io.github.henaorth16.kenat;

/**
 * Canonical tag constants for classifying holidays and fasting periods.
 * Mirrors {@code HolidayTags} in {@code constants.ts}.
 */
public final class HolidayTags {

    public static final String PUBLIC    = "public";
    public static final String RELIGIOUS = "religious";
    public static final String CHRISTIAN = "christian";
    public static final String MUSLIM    = "muslim";
    public static final String STATE     = "state";
    public static final String CULTURAL  = "cultural";
    public static final String OTHER     = "other";

    /**
     * Marks a monthly Orthodox commemoration that is a <em>nigs</em> (major
     * feast) in the month being rendered.  Applied by {@code MonthGrid} in
     * {@code "christian"} mode; mirrors the {@code 'NIGS'} tag in
     * {@code MonthGrid.ts}.
     */
    public static final String NIGS = "NIGS";

    /**
     * Marks a monthly Orthodox commemoration that is <em>not</em> a nigs in the
     * month being rendered.  Only surfaced when
     * {@code MonthGridConfig.showAllSaints} is enabled; mirrors the
     * {@code 'SAINT_DAY'} tag in {@code MonthGrid.ts}.
     */
    public static final String SAINT_DAY = "SAINT_DAY";

    private HolidayTags() {}
}
