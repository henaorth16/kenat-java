package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidDateFormatException;
import io.github.henaorth16.kenat.exception.InvalidEthiopianDateException;
import io.github.henaorth16.kenat.exception.UnrecognizedInputException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Main entry point for the Kenat Ethiopian calendar library.
 * Mirrors the {@code Kenat} class in {@code Kenat.ts}.
 *
 * <p>A {@code Kenat} instance wraps an Ethiopian date and an optional
 * {@link EthiopianTime}.  It is immutable — all arithmetic methods return
 * new instances.
 *
 * <h2>Creating instances</h2>
 * <pre>{@code
 * Kenat k = Kenat.now();                         // current date/time
 * Kenat k = new Kenat("2016/1/1");               // from string
 * Kenat k = new Kenat(new EthiopianDate(2016,1,1));
 * Kenat k = new Kenat(LocalDate.of(2023, 9, 12));// from Gregorian
 * }</pre>
 *
 * <h2>Method chaining</h2>
 * <pre>{@code
 * String s = Kenat.now()
 *                 .add(1, "months")
 *                 .format("amharic");
 * }</pre>
 */
public final class Kenat {

    private final EthiopianDate ethiopian;
    private final EthiopianTime time;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a {@code Kenat} for today's date (converted from system clock).
     */
    public Kenat() {
        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();
        this.ethiopian  = Conversions.toEC(today);
        this.time       = EthiopianTime.fromGregorian(now.getHour(), now.getMinute());
    }

    /**
     * Creates a {@code Kenat} from an Ethiopian date string.
     * Accepted formats: {@code "yyyy/mm/dd"} or {@code "yyyy-mm-dd"}.
     *
     * @param input date string
     * @throws InvalidDateFormatException if the string is malformed
     * @throws InvalidEthiopianDateException if the date is out of range
     */
    public Kenat(String input) {
        this(parse(input), defaultTime());
    }

    /**
     * Creates a {@code Kenat} from an Ethiopian date.
     *
     * @param date Ethiopian date
     */
    public Kenat(EthiopianDate date) {
        this(date, defaultTime());
    }

    /**
     * Creates a {@code Kenat} from an Ethiopian date with a specific time.
     *
     * @param date Ethiopian date
     * @param time Ethiopian time
     */
    public Kenat(EthiopianDate date, EthiopianTime time) {
        Objects.requireNonNull(date, "date must not be null");
        if (!EthiopianCalendarUtils.isValidEthiopianDate(date.year(), date.month(), date.day())) {
            throw new InvalidEthiopianDateException(date.year(), date.month(), date.day());
        }
        this.ethiopian = date;
        this.time      = time != null ? time : defaultTime();
    }

    /**
     * Creates a {@code Kenat} from a Gregorian {@link LocalDate}.
     *
     * @param date Gregorian date
     */
    public Kenat(LocalDate date) {
        this(Conversions.toEC(date), defaultTime());
    }

    // -----------------------------------------------------------------------
    // Static factories
    // -----------------------------------------------------------------------

    /** Returns a new {@code Kenat} representing the current date and time. */
    public static Kenat now() {
        return new Kenat();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /** Returns the Ethiopian date. */
    public EthiopianDate getEthiopian() {
        return ethiopian;
    }

    /** Returns the corresponding Gregorian date. */
    public GregorianDate getGregorian() {
        return Conversions.toGC(ethiopian);
    }

    /** Returns the time component. */
    public EthiopianTime getTime() {
        return time;
    }

    /**
     * Returns the date in the specified calendar system.
     *
     * @param calendar {@code "ethiopian"} (default) or {@code "gregorian"}
     */
    public Object getDate(String calendar) {
        return "gregorian".equalsIgnoreCase(calendar) ? getGregorian() : getEthiopian();
    }

    // -----------------------------------------------------------------------
    // Time setter
    // -----------------------------------------------------------------------

    /**
     * Returns a new {@code Kenat} with the given time set.
     *
     * @param hour   Ethiopian hour (1–12)
     * @param minute minute (0–59)
     * @param period {@link TimePeriod#DAY} or {@link TimePeriod#NIGHT}
     * @return new instance with updated time
     */
    public Kenat setTime(int hour, int minute, TimePeriod period) {
        return new Kenat(ethiopian, new EthiopianTime(hour, minute, period));
    }

    // -----------------------------------------------------------------------
    // Bahire Hasab
    // -----------------------------------------------------------------------

    /**
     * Calculates Bahire Hasab values for this instance's year.
     *
     * @return full {@link BahireHasab.BahireHasabResult}
     */
    public BahireHasab.BahireHasabResult getBahireHasab() {
        return BahireHasab.getBahireHasab(ethiopian.year());
    }

    // -----------------------------------------------------------------------
    // Formatting
    // -----------------------------------------------------------------------

    /**
     * Returns a string like {@code "መስከረም 1 2016 12:00 ጠዋት"}.
     * Pass {@code "gregorian"} to get the Gregorian equivalent.
     *
     * @param calendar {@code "ethiopian"} (default) or {@code "gregorian"}
     * @return formatted string
     */
    public String toString(String calendar) {
        if ("gregorian".equalsIgnoreCase(calendar)) {
            return Formatting.formatGregorianWithTime(getGregorian(), time, "amharic");
        }
        return Formatting.formatWithTime(ethiopian, time, "amharic");
    }

    @Override
    public String toString() {
        return toString("ethiopian");
    }

    /**
     * Flexible date formatter.
     *
     * @param calendar    {@code "ethiopian"} or {@code "gregorian"}
     * @param lang        {@code "amharic"} or {@code "english"}
     * @param showWeekday prepend weekday name
     * @param useGeez     display numerals in Ge'ez
     * @param includeTime append time string
     * @return formatted string
     */
    public String format(String calendar, String lang, boolean showWeekday,
                         boolean useGeez, boolean includeTime) {
        if ("gregorian".equalsIgnoreCase(calendar)) {
            GregorianDate g = getGregorian();
            if (showWeekday && includeTime) {
                return Formatting.formatGregorianWithWeekday(g, lang)
                        + " " + time.format(lang, false, true, false);
            }
            if (showWeekday)   return Formatting.formatGregorianWithWeekday(g, lang);
            if (includeTime)   return Formatting.formatGregorianWithTime(g, time, lang);
            return Formatting.formatGregorianStandard(g);
        }

        // Ethiopian
        if (showWeekday && includeTime) {
            return Formatting.formatWithWeekday(ethiopian, lang, useGeez)
                    + " " + time.format(lang, useGeez, true, false);
        }
        if (showWeekday)   return Formatting.formatWithWeekday(ethiopian, lang, useGeez);
        if (includeTime)   return Formatting.formatWithTime(ethiopian, time, lang);
        if (useGeez && "amharic".equalsIgnoreCase(lang))
            return Formatting.formatInGeezAmharic(ethiopian);
        return Formatting.formatStandard(ethiopian, lang);
    }

    /** Formats with default Amharic, no weekday, no Ge'ez numerals, no time. */
    public String format() {
        return format("ethiopian", "amharic", false, false, false);
    }

    /** Formats with the given language, other options defaulting to false. */
    public String format(String lang) {
        return format("ethiopian", lang, false, false, false);
    }

    /** Formats in Ge'ez numerals with Amharic month name. */
    public String formatInGeezAmharic() {
        return Formatting.formatInGeezAmharic(ethiopian);
    }

    /** Formats with weekday name. */
    public String formatWithWeekday(String lang, boolean useGeez) {
        return Formatting.formatWithWeekday(ethiopian, lang, useGeez);
    }

    /** Returns {@code "yyyy/mm/dd"} short format. */
    public String formatShort() {
        return Formatting.formatShort(ethiopian);
    }

    /**
     * Returns an ISO-style date string.
     *
     * @param calendar {@code "ethiopian"} (custom ISO-like) or {@code "gregorian"} (true ISO 8601)
     * @return ISO string
     */
    public String toISOString(String calendar) {
        if ("gregorian".equalsIgnoreCase(calendar)) {
            return Formatting.toGregorianISODateString(getGregorian(), time);
        }
        return Formatting.toISODateString(ethiopian, time);
    }

    /** Returns the Ethiopian ISO-like date string without time. */
    public String toISOString() {
        return toISOString("ethiopian");
    }

    // -----------------------------------------------------------------------
    // Holiday check
    // -----------------------------------------------------------------------

    /**
     * Checks whether this date is a holiday.
     *
     * @param lang language for names
     * @return list of holidays on this date (empty if none)
     */
    public List<Holiday> isHoliday(String lang) {
        List<Holiday> all = Holidays.getHolidaysInMonth(
                ethiopian.year(), ethiopian.month(), lang, null);
        List<Holiday> today = new ArrayList<>();
        for (Holiday h : all) {
            if (h.ethiopian().day() == ethiopian.day()) today.add(h);
        }
        return today;
    }

    /** Checks holidays with Amharic names. */
    public List<Holiday> isHoliday() {
        return isHoliday("amharic");
    }

    // -----------------------------------------------------------------------
    // Calendar grid
    // -----------------------------------------------------------------------

    /**
     * Generates the calendar for this instance's month.
     *
     * @param useGeez display in Ge'ez numerals
     * @return list of day entries
     */
    public List<CalendarDay> getMonthCalendar(boolean useGeez) {
        return buildMonthCalendar(ethiopian.year(), ethiopian.month(), useGeez);
    }

    /** Generates calendar for an arbitrary Ethiopian month. */
    public static List<CalendarDay> getMonthCalendarStatic(int year, int month, boolean useGeez) {
        return buildMonthCalendar(year, month, useGeez);
    }

    /**
     * Generates a full-grid MonthGridResult for the given year/month.
     *
     * @param year   Ethiopian year
     * @param month  Ethiopian month
     * @param config optional grid config (weekStart, lang, mode, filter, geez)
     * @return {@link MonthGrid.MonthGridResult}
     */
    public static MonthGrid.MonthGridResult getMonthGrid(int year, int month,
                                                          MonthGrid.MonthGridConfig config) {
        if (config == null) config = new MonthGrid.MonthGridConfig(year, month);
        return MonthGrid.create(config);
    }

    /** Convenience with default config. */
    public static MonthGrid.MonthGridResult getMonthGrid(int year, int month) {
        return MonthGrid.create(year, month);
    }

    /**
     * Generates a full-year grid (13 months).
     *
     * @param year   Ethiopian year
     * @param config optional grid config (applied per month)
     * @return list of 13 {@link MonthGrid.MonthGridResult} objects
     */
    public static List<MonthGrid.MonthGridResult> getYearCalendar(int year,
                                                                    MonthGrid.MonthGridConfig config) {
        List<MonthGrid.MonthGridResult> fullYear = new ArrayList<>(13);
        for (int m = 1; m <= 13; m++) {
            MonthGrid.MonthGridConfig mc = (config != null)
                    ? new MonthGrid.MonthGridConfig(year, m, config.weekStart(),
                            config.useGeez(), config.weekdayLang(),
                            config.holidayFilter(), config.mode(), config.showAllSaints())
                    : new MonthGrid.MonthGridConfig(year, m);
            fullYear.add(MonthGrid.create(mc));
        }
        return fullYear;
    }

    /** Convenience with default config. */
    public static List<MonthGrid.MonthGridResult> getYearCalendar(int year) {
        return getYearCalendar(year, null);
    }

    /**
     * Generates a range of Kenat instances from start to end (inclusive).
     *
     * @param start range start
     * @param end   range end
     * @return ordered list of Kenat instances; empty if start is after end
     */
    public static List<Kenat> generateDateRange(Kenat start, Kenat end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end,   "end must not be null");

        if (start.isAfter(end)) return List.of();

        List<Kenat> range = new ArrayList<>();
        Kenat current = start;
        while (!current.isAfter(end)) {
            range.add(current);
            current = current.addDays(1);
        }
        return range;
    }

    // -----------------------------------------------------------------------
    // Arithmetic
    // -----------------------------------------------------------------------

    /**
     * Adds an amount to this date and returns a new {@code Kenat}.
     *
     * @param amount amount to add (may be negative)
     * @param unit   {@code "days"}, {@code "months"}, or {@code "years"}
     * @return new {@code Kenat}
     */
    public Kenat add(int amount, String unit) {
        EthiopianDate newDate = switch (unit) {
            case "days"   -> DayArithmetic.addDays(ethiopian, amount);
            case "months" -> DayArithmetic.addMonths(ethiopian, amount);
            case "years"  -> DayArithmetic.addYears(ethiopian, amount);
            default -> throw new IllegalArgumentException(
                    "Invalid unit: " + unit + ". Use 'days', 'months', or 'years'.");
        };
        return new Kenat(newDate, time);
    }

    /** Subtracts an amount from this date. */
    public Kenat subtract(int amount, String unit) {
        return add(-amount, unit);
    }

    /** Adds {@code days} to this date. */
    public Kenat addDays(int days) {
        return add(days, "days");
    }

    /** Adds {@code months} to this date. */
    public Kenat addMonths(int months) {
        return add(months, "months");
    }

    /** Adds {@code years} to this date. */
    public Kenat addYears(int years) {
        return add(years, "years");
    }

    /** Returns the signed difference in days between this and another Kenat. */
    public int diffInDays(Kenat other) {
        return DayArithmetic.diffInDays(ethiopian, other.getEthiopian());
    }

    /** Returns the signed difference in months between this and another Kenat. */
    public int diffInMonths(Kenat other) {
        return DayArithmetic.diffInMonths(ethiopian, other.getEthiopian());
    }

    /** Returns the signed difference in years between this and another Kenat. */
    public int diffInYears(Kenat other) {
        return DayArithmetic.diffInYears(ethiopian, other.getEthiopian());
    }

    // -----------------------------------------------------------------------
    // Comparison
    // -----------------------------------------------------------------------

    /** Returns {@code true} if this date is before {@code other}. */
    public boolean isBefore(Kenat other) {
        return diffInDays(other) < 0;
    }

    /** Returns {@code true} if this date is after {@code other}. */
    public boolean isAfter(Kenat other) {
        return diffInDays(other) > 0;
    }

    /** Returns {@code true} if this date is the same day as {@code other}. */
    public boolean isSameDay(Kenat other) {
        EthiopianDate o = other.getEthiopian();
        return ethiopian.year() == o.year()
                && ethiopian.month() == o.month()
                && ethiopian.day() == o.day();
    }

    /** Returns {@code true} if this date is in the same month and year as {@code other}. */
    public boolean isSameMonth(Kenat other) {
        EthiopianDate o = other.getEthiopian();
        return ethiopian.year() == o.year() && ethiopian.month() == o.month();
    }

    /** Returns {@code true} if this date is in the same year as {@code other}. */
    public boolean isSameYear(Kenat other) {
        return ethiopian.year() == other.getEthiopian().year();
    }

    // -----------------------------------------------------------------------
    // Start/end of period
    // -----------------------------------------------------------------------

    /**
     * Returns a new {@code Kenat} set to the start of the specified unit.
     *
     * @param unit {@code "day"}, {@code "month"}, or {@code "year"}
     * @return new instance
     */
    public Kenat startOf(String unit) {
        return switch (unit) {
            case "day"   -> new Kenat(ethiopian, new EthiopianTime(12, 0, TimePeriod.DAY));
            case "month" -> new Kenat(new EthiopianDate(ethiopian.year(), ethiopian.month(), 1), time);
            case "year"  -> new Kenat(new EthiopianDate(ethiopian.year(), 1, 1), time);
            default -> throw new IllegalArgumentException("Invalid unit: " + unit);
        };
    }

    /**
     * Returns a new {@code Kenat} set to the end of the specified unit.
     *
     * @param unit {@code "day"}, {@code "month"}, or {@code "year"}
     * @return new instance
     */
    public Kenat endOf(String unit) {
        return switch (unit) {
            case "day"   -> new Kenat(ethiopian, new EthiopianTime(12, 0, TimePeriod.NIGHT));
            case "month" -> {
                int last = EthiopianCalendarUtils.getEthiopianDaysInMonth(ethiopian.year(), ethiopian.month());
                yield new Kenat(new EthiopianDate(ethiopian.year(), ethiopian.month(), last), time);
            }
            case "year"  -> {
                int last = EthiopianCalendarUtils.getEthiopianDaysInMonth(ethiopian.year(), 13);
                yield new Kenat(new EthiopianDate(ethiopian.year(), 13, last), time);
            }
            default -> throw new IllegalArgumentException("Invalid unit: " + unit);
        };
    }

    // -----------------------------------------------------------------------
    // JSON / serialization
    // -----------------------------------------------------------------------

    /**
     * Returns a map representation suitable for JSON serialization.
     *
     * @return map with {@code "ethiopian"}, {@code "gregorian"}, and {@code "time"} keys
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ethiopian", Map.of(
                "year", ethiopian.year(),
                "month", ethiopian.month(),
                "day", ethiopian.day()));
        GregorianDate g = getGregorian();
        map.put("gregorian", Map.of(
                "year", g.year(),
                "month", g.month(),
                "day", g.day()));
        if (time != null) {
            map.put("time", Map.of(
                    "hour", time.hour(),
                    "minute", time.minute(),
                    "period", time.period().key()));
        } else {
            map.put("time", null);
        }
        return Collections.unmodifiableMap(map);
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static EthiopianDate parse(String input) {
        if (input == null || input.isBlank())
            throw new InvalidDateFormatException(input);
        String[] parts = input.split("[/\\-]");
        if (parts.length != 3) throw new InvalidDateFormatException(input);
        try {
            int year  = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim());
            int day   = Integer.parseInt(parts[2].trim());
            return new EthiopianDate(year, month, day);
        } catch (NumberFormatException e) {
            throw new InvalidDateFormatException(input);
        }
    }

    private static EthiopianTime defaultTime() {
        return new EthiopianTime(12, 0, TimePeriod.DAY);
    }

    private static List<CalendarDay> buildMonthCalendar(int year, int month, boolean useGeez) {
        int daysInMonth = EthiopianCalendarUtils.getEthiopianDaysInMonth(year, month);
        List<CalendarDay> calendar = new ArrayList<>(daysInMonth);
        String amMonthName = KenatConstants.MONTH_NAMES_AMHARIC.get(month - 1);
        for (int day = 1; day <= daysInMonth; day++) {
            EthiopianDate eth = new EthiopianDate(year, month, day);
            GregorianDate greg = Conversions.toGC(eth);
            String ethDisplay = useGeez
                    ? amMonthName + " " + GeezConverter.toGeez(day) + " " + GeezConverter.toGeez(year)
                    : amMonthName + " " + day + " " + year;
            String gregDisplay = String.format("%d-%02d-%02d", greg.year(), greg.month(), greg.day());
            calendar.add(new CalendarDay(eth, ethDisplay, greg, gregDisplay));
        }
        return calendar;
    }

    // -----------------------------------------------------------------------
    // CalendarDay result type
    // -----------------------------------------------------------------------

    /**
     * A single day entry in an instance-level calendar listing.
     *
     * @param ethiopian     Ethiopian date
     * @param ethDisplay    formatted Ethiopian display string
     * @param gregorian     Gregorian date
     * @param gregDisplay   formatted Gregorian display string
     */
    public record CalendarDay(
            EthiopianDate ethiopian,
            String ethDisplay,
            GregorianDate gregorian,
            String gregDisplay
    ) {}
}
