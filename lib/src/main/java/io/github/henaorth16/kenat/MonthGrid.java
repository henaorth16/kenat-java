package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidGridConfigException;

import java.time.LocalDate;
import java.util.*;

/**
 * Generates a calendar grid for a given Ethiopian month.
 * Mirrors the {@code MonthGrid} class in {@code MonthGrid.ts}.
 *
 * <p>Usage:
 * <pre>{@code
 * MonthGridResult grid = MonthGrid.create(new MonthGridConfig(2016, 1));
 * grid.headers();   // weekday header row
 * grid.days();      // list of day cells (null = padding before first day)
 * grid = grid.up(); // same grid advanced one month
 * }</pre>
 *
 * <p>The {@code mode} setting drives which entries land on each day cell:
 * <ul>
 *   <li>{@code "christian"} — Christian holidays plus the Orthodox monthly
 *       commemorations from {@link OrthodoxCommemorations}</li>
 *   <li>{@code "muslim"} — Muslim holidays plus a Jummah entry on every Friday</li>
 *   <li>{@code "public"} — public holidays only</li>
 *   <li>{@code null} — no mode filter; {@code holidayFilter} applies as given</li>
 * </ul>
 */
public final class MonthGrid {

    private MonthGrid() {}

    /** Languages accepted for {@code weekdayLang}; mirrors the keys of {@code daysOfWeek} in TS. */
    private static final Set<String> SUPPORTED_LANGS = Set.of("amharic", "english");

    // -----------------------------------------------------------------------
    // Config record
    // -----------------------------------------------------------------------

    /**
     * Configuration for month-grid generation.
     *
     * @param year          Ethiopian year (required with month)
     * @param month         Ethiopian month 1–13 (required with year)
     * @param weekStart     first day of week: 0=Sunday … 6=Saturday (default 1=Monday)
     * @param useGeez       display dates in Ge'ez numerals (default false)
     * @param weekdayLang   language for weekday/month headers (default "amharic")
     * @param holidayFilter tag filter for holidays (null = no filter)
     * @param mode          short-hand filter: "christian", "muslim", "public", or null
     * @param showAllSaints include all saints even outside their nigs month
     */
    public record MonthGridConfig(
            Integer year,
            Integer month,
            int weekStart,
            boolean useGeez,
            String weekdayLang,
            List<String> holidayFilter,
            String mode,
            boolean showAllSaints
    ) {
        /** Convenience constructor with just year and month. */
        public MonthGridConfig(int year, int month) {
            this(year, month, 1, false, "amharic", null, null, false);
        }

        /** Default config — uses current Ethiopian date. */
        public MonthGridConfig() {
            this(null, null, 1, false, "amharic", null, null, false);
        }

        /** Returns a copy of this config pointing at a different year/month. */
        public MonthGridConfig withYearMonth(int newYear, int newMonth) {
            return new MonthGridConfig(newYear, newMonth, weekStart, useGeez,
                    weekdayLang, holidayFilter, mode, showAllSaints);
        }
    }

    // -----------------------------------------------------------------------
    // Result types
    // -----------------------------------------------------------------------

    /**
     * A single day cell in the grid.
     *
     * @param ethiopian Ethiopian date representation
     * @param gregorian Gregorian date
     * @param weekday   0=Sunday … 6=Saturday
     * @param weekdayName localized weekday name
     * @param isToday   whether this cell is today's date
     * @param holidays  holidays and commemorations on this day
     */
    public record MonthGridDay(
            EthiopianDateDisplay ethiopian,
            GregorianDate gregorian,
            int weekday,
            String weekdayName,
            boolean isToday,
            List<Holiday> holidays
    ) {
        /** Canonical constructor — makes the holidays list immutable. */
        public MonthGridDay {
            holidays = holidays == null ? List.of() : List.copyOf(holidays);
        }
    }

    /**
     * Displayable Ethiopian date (day/month/year may be Ge'ez strings or integers).
     */
    public record EthiopianDateDisplay(
            String year,
            String month,
            String day
    ) {}

    /**
     * Complete result of generating a month grid.
     *
     * @param headers  weekday header labels (rotated by weekStart)
     * @param days     grid cells — null entries are padding before the first day
     * @param year     year label (number or Ge'ez string)
     * @param month    month number
     * @param monthName localized month name
     * @param config   the configuration this grid was generated from
     */
    public record MonthGridResult(
            List<String> headers,
            List<MonthGridDay> days,   // null entries are padding
            String year,
            int month,
            String monthName,
            MonthGridConfig config
    ) {
        /**
         * Backwards-compatible constructor for callers written against the
         * pre-{@code config} shape of this record. {@link #up()} and
         * {@link #down()} cannot be used on a grid built this way unless the
         * year label is numeric.
         */
        public MonthGridResult(List<String> headers, List<MonthGridDay> days,
                               String year, int month, String monthName) {
            this(headers, days, year, month, monthName, null);
        }

        /**
         * Returns the grid for the following month, keeping every other setting.
         * Rolls Pagume (13) over into Meskerem (1) of the next year.
         *
         * @return a newly generated {@code MonthGridResult}
         */
        public MonthGridResult up() {
            int y = numericYear();
            int m = month;
            if (m == 13) { m = 1; y++; } else { m++; }
            return create(baseConfig().withYearMonth(y, m));
        }

        /**
         * Returns the grid for the preceding month, keeping every other setting.
         * Rolls Meskerem (1) back into Pagume (13) of the previous year.
         *
         * @return a newly generated {@code MonthGridResult}
         */
        public MonthGridResult down() {
            int y = numericYear();
            int m = month;
            if (m == 1) { m = 13; y--; } else { m--; }
            return create(baseConfig().withYearMonth(y, m));
        }

        /** Returns this grid's config, or a default one when it was built without. */
        private MonthGridConfig baseConfig() {
            return config != null ? config : new MonthGridConfig(numericYear(), month);
        }

        /**
         * Returns the grid's Ethiopian year as a number.
         *
         * <p>{@link #year()} is a display label and may hold Ge'ez numerals when
         * {@code useGeez} is set; this accessor always returns the numeric value.
         *
         * @return Ethiopian year
         */
        public int numericYear() {
            if (config != null && config.year() != null) return config.year();
            try {
                return Integer.parseInt(year);
            } catch (NumberFormatException e) {
                return GeezConverter.toArabic(year);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Factory
    // -----------------------------------------------------------------------

    /**
     * Creates a month grid using the given configuration.
     *
     * @param config grid configuration
     * @return {@link MonthGridResult}
     * @throws InvalidGridConfigException if the config is invalid
     */
    public static MonthGridResult create(MonthGridConfig config) {
        if (config == null) config = new MonthGridConfig();
        validate(config);

        // Resolve year/month defaults to today
        EthiopianDate today = Conversions.toEC(LocalDate.now());
        int year  = config.year()  != null ? config.year()  : today.year();
        int month = config.month() != null ? config.month() : today.month();
        String lang = config.weekdayLang();

        // Build holiday filter
        List<String> filter = buildFilter(config);

        // Fetch holidays for the month
        List<Holiday> holidays = Holidays.getHolidaysInMonth(year, month, lang, filter);

        // Orthodox monthly commemorations, keyed by day of month (christian mode only)
        Map<Integer, List<Holiday>> saintsByDay = buildSaintsMap(year, month, config, lang);

        // Build day cells
        List<MonthGridDay> rawDays = buildDays(year, month, config, holidays, saintsByDay, today, lang);

        // Pad the front with nulls according to weekStart
        List<MonthGridDay> paddedDays = padDays(rawDays, config.weekStart());

        // Build headers
        List<String> headers = buildHeaders(lang, config.weekStart());

        // Localized month name
        List<String> monthNames = "english".equalsIgnoreCase(lang)
                ? KenatConstants.MONTH_NAMES_ENGLISH
                : KenatConstants.MONTH_NAMES_AMHARIC;
        String monthName = monthNames.get(month - 1);
        String yearLabel = config.useGeez() ? GeezConverter.toGeez(year) : String.valueOf(year);

        return new MonthGridResult(headers, paddedDays, yearLabel, month, monthName,
                config.withYearMonth(year, month));
    }

    /** Convenience factory with just year and month. */
    public static MonthGridResult create(int year, int month) {
        return create(new MonthGridConfig(year, month));
    }

    /** Convenience factory using today's Ethiopian year and month. */
    public static MonthGridResult create() {
        return create(new MonthGridConfig());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List<MonthGridDay> buildDays(int year, int month,
                                                 MonthGridConfig config,
                                                 List<Holiday> holidays,
                                                 Map<Integer, List<Holiday>> saintsByDay,
                                                 EthiopianDate today,
                                                 String lang) {
        int daysInMonth = EthiopianCalendarUtils.getEthiopianDaysInMonth(year, month);

        // Build holiday index keyed by day
        Map<Integer, List<Holiday>> holidaysByDay = new HashMap<>();
        for (Holiday h : holidays) {
            if (h.ethiopian().year() == year && h.ethiopian().month() == month) {
                holidaysByDay.computeIfAbsent(h.ethiopian().day(), k -> new ArrayList<>()).add(h);
            }
        }

        List<String> weekdayNames = "english".equalsIgnoreCase(lang)
                ? KenatConstants.DAYS_OF_WEEK_ENGLISH
                : KenatConstants.DAYS_OF_WEEK_AMHARIC;

        boolean muslimMode = "muslim".equals(config.mode());
        boolean christianMode = "christian".equals(config.mode());

        List<MonthGridDay> days = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            EthiopianDate eth = new EthiopianDate(year, month, day);
            GregorianDate greg = Conversions.toGC(eth);
            int weekday = EthiopianCalendarUtils.getWeekday(eth);
            String weekdayName = weekdayNames.get(weekday);

            // Ge'ez labels
            String dayLabel;
            String monthLabel;
            String yearLabel;
            if (config.useGeez()) {
                dayLabel   = GeezConverter.toGeez(day);
                monthLabel = ("english".equalsIgnoreCase(lang)
                        ? KenatConstants.MONTH_NAMES_ENGLISH
                        : KenatConstants.MONTH_NAMES_AMHARIC).get(month - 1);
                yearLabel  = GeezConverter.toGeez(year);
            } else {
                dayLabel   = String.valueOf(day);
                monthLabel = String.valueOf(month);
                yearLabel  = String.valueOf(year);
            }

            boolean isToday = eth.year() == today.year()
                    && eth.month() == today.month()
                    && eth.day() == today.day();

            List<Holiday> dayHolidays = new ArrayList<>(holidaysByDay.getOrDefault(day, List.of()));

            if (christianMode) {
                dayHolidays.addAll(saintsByDay.getOrDefault(day, List.of()));
            }

            if (muslimMode && weekday == 5) {
                dayHolidays.add(jummah(eth, greg, lang));
            }

            days.add(new MonthGridDay(
                    new EthiopianDateDisplay(yearLabel, monthLabel, dayLabel),
                    greg,
                    weekday,
                    weekdayName,
                    isToday,
                    dayHolidays
            ));
        }
        return days;
    }

    /**
     * Builds the day-of-month → commemorations index for {@code "christian"} mode.
     *
     * <p>By default only commemorations whose {@code negs} list contains the
     * rendered month (the nigs days) are included; {@code showAllSaints} widens
     * this to every commemoration recorded for the day.
     */
    private static Map<Integer, List<Holiday>> buildSaintsMap(int year, int month,
                                                               MonthGridConfig config,
                                                               String lang) {
        if (!"christian".equals(config.mode())) return Map.of();

        int daysInMonth = EthiopianCalendarUtils.getEthiopianDaysInMonth(year, month);
        Map<Integer, List<Holiday>> map = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<OrthodoxCommemorations.Commemoration>> entry
                : OrthodoxCommemorations.byDay().entrySet()) {
            int day = entry.getKey();
            if (day < 1 || day > daysInMonth) continue;

            EthiopianDate eth = new EthiopianDate(year, month, day);
            GregorianDate greg = Conversions.toGC(eth);

            for (OrthodoxCommemorations.Commemoration c : entry.getValue()) {
                boolean isNigs = c.isNigsIn(month);
                if (!isNigs && !config.showAllSaints()) continue;

                map.computeIfAbsent(day, k -> new ArrayList<>()).add(new Holiday(
                        c.id(),
                        List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN,
                                isNigs ? HolidayTags.NIGS : HolidayTags.SAINT_DAY),
                        false,
                        resolve(c.name(), lang),
                        resolve(c.description(), lang),
                        eth,
                        greg
                ));
            }
        }
        return map;
    }

    /** Builds the weekly Jummah entry attached to every Friday in {@code "muslim"} mode. */
    private static Holiday jummah(EthiopianDate eth, GregorianDate greg, String lang) {
        KenatConstants.HolidayInfo info = KenatConstants.HOLIDAY_INFO.get("jummah");
        return new Holiday(
                "jummah",
                List.of(HolidayTags.RELIGIOUS, HolidayTags.MUSLIM),
                false,
                info != null ? resolve(info.name(), lang) : "jummah",
                info != null ? resolve(info.description(), lang) : "",
                eth,
                greg
        );
    }

    /**
     * Pads the day list with {@code null} entries at the front so the first
     * cell falls on the correct column for the given weekStart.
     *
     * <p>The returned list uses {@code null} for padding slots (matching the
     * TypeScript implementation).  Callers rendering a 7-column grid should
     * treat null as an empty cell.
     */
    private static List<MonthGridDay> padDays(List<MonthGridDay> rawDays, int weekStart) {
        if (rawDays.isEmpty()) return rawDays;
        int firstWeekday = rawDays.get(0).weekday();
        int offset = Math.floorMod(firstWeekday - weekStart, 7);
        List<MonthGridDay> result = new ArrayList<>(offset + rawDays.size());
        for (int i = 0; i < offset; i++) result.add(null);
        result.addAll(rawDays);
        return result;
    }

    private static List<String> buildHeaders(String lang, int weekStart) {
        List<String> all = "english".equalsIgnoreCase(lang)
                ? KenatConstants.DAYS_OF_WEEK_ENGLISH
                : KenatConstants.DAYS_OF_WEEK_AMHARIC;
        List<String> headers = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            headers.add(all.get((weekStart + i) % 7));
        }
        return headers;
    }

    private static List<String> buildFilter(MonthGridConfig config) {
        if (config.mode() != null) {
            return switch (config.mode()) {
                case "christian" -> List.of(HolidayTags.CHRISTIAN);
                case "muslim"    -> List.of(HolidayTags.MUSLIM);
                case "public"    -> List.of(HolidayTags.PUBLIC);
                default -> config.holidayFilter();
            };
        }
        return config.holidayFilter();
    }

    private static void validate(MonthGridConfig config) {
        boolean hasYear  = config.year()  != null;
        boolean hasMonth = config.month() != null;
        if (hasYear != hasMonth) {
            throw new InvalidGridConfigException(
                    "If providing year or month, both must be provided.");
        }
        if (hasMonth && (config.month() < 1 || config.month() > 13)) {
            throw new InvalidGridConfigException(
                    "Invalid month: " + config.month() + ". Must be 1–13.");
        }
        int ws = config.weekStart();
        if (ws < 0 || ws > 6) {
            throw new InvalidGridConfigException(
                    "Invalid weekStart: " + ws + ". Must be 0–6.");
        }
        String lang = config.weekdayLang();
        if (lang == null || !SUPPORTED_LANGS.contains(lang.toLowerCase(Locale.ROOT))) {
            throw new InvalidGridConfigException(
                    "Invalid weekdayLang: \"" + lang + "\". Must be one of "
                            + new TreeSet<>(SUPPORTED_LANGS) + ".");
        }
    }

    private static String resolve(LocalizedText text, String lang) {
        return "english".equalsIgnoreCase(lang) ? text.english() : text.amharic();
    }
}
