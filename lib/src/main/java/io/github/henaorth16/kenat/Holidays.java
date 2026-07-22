package io.github.henaorth16.kenat;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holiday lookup for Ethiopian calendar.
 * Mirrors {@code holidays.ts} from the TypeScript implementation.
 *
 * <p>Fixed (non-movable) holidays have a constant month/day each Ethiopian year.
 * Movable Christian feasts are derived from Bahire Hasab.
 * Islamic holidays are approximated using a 354/355-day Hijri cycle.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class Holidays {

    private Holidays() {}

    // -----------------------------------------------------------------------
    // Fixed holiday registry (mirrors fixedHolidays in holidays.ts)
    // -----------------------------------------------------------------------

    private record FixedEntry(int month, int day, List<String> tags) {}

    private static final Map<String, FixedEntry> FIXED = Map.ofEntries(
            Map.entry("enkutatash",    new FixedEntry(1,  1,  List.of(HolidayTags.PUBLIC, HolidayTags.CULTURAL))),
            Map.entry("meskel",        new FixedEntry(1,  17, List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN))),
            Map.entry("beherbehereseb",new FixedEntry(3,  29, List.of(HolidayTags.PUBLIC, HolidayTags.STATE))),
            Map.entry("gena",          new FixedEntry(4,  29, List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN))),
            Map.entry("timket",        new FixedEntry(5,  11, List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN))),
            Map.entry("martyrsDay",    new FixedEntry(6,  12, List.of(HolidayTags.PUBLIC, HolidayTags.STATE))),
            Map.entry("adwa",          new FixedEntry(6,  23, List.of(HolidayTags.PUBLIC, HolidayTags.STATE))),
            Map.entry("labour",        new FixedEntry(8,  23, List.of(HolidayTags.PUBLIC, HolidayTags.STATE))),
            Map.entry("patriots",      new FixedEntry(8,  27, List.of(HolidayTags.PUBLIC, HolidayTags.STATE)))
    );

    // -----------------------------------------------------------------------
    // Islamic constants — Hijri epoch in Julian Day Number
    // -----------------------------------------------------------------------

    /** Julian Day Number of the Hijri epoch (1 Muharram 1 AH = 16 July 622 CE). */
    private static final long HIJRI_EPOCH_JDN = 1948438L;
    private static final int[] HIJRI_MONTH_DAYS = {30,29,30,29,30,29,30,29,30,29,30,29};

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns the holiday for the given key and Ethiopian year, or {@code null}.
     *
     * @param holidayKey camelCase holiday key (e.g. {@code "fasika"})
     * @param ethYear    Ethiopian year
     * @param lang       {@code "amharic"} or {@code "english"}
     * @return {@link Holiday} or {@code null} if the key is not recognised
     */
    public static Holiday getHoliday(String holidayKey, int ethYear, String lang) {
        KenatConstants.HolidayInfo info = KenatConstants.HOLIDAY_INFO.get(holidayKey);
        if (info == null) return null;

        String name = resolve(info.name(), lang);
        String desc = resolve(info.description(), lang);

        // Fixed holiday?
        FixedEntry fixed = FIXED.get(holidayKey);
        if (fixed != null) {
            return new Holiday(holidayKey, fixed.tags(), false, name, desc,
                    new EthiopianDate(ethYear, fixed.month(), fixed.day()), null);
        }

        // Movable Christian feast?
        String tewsakKey = KenatConstants.KEY_TO_TEWSAK_MAP.get(holidayKey);
        if (tewsakKey != null) {
            EthiopianDate date = BahireHasab.getMovableHoliday(tewsakKey, ethYear);
            List<String> tags  = KenatConstants.MOVABLE_HOLIDAYS.getOrDefault(holidayKey, List.of());
            GregorianDate greg = Conversions.toGC(date);
            return new Holiday(holidayKey, tags, true, name, desc, date, greg);
        }

        // Islamic holidays?
        List<IslamicOccurrence> occurrences = switch (holidayKey) {
            case "eidFitr"  -> findIslamicOccurrences(ethYear, 10, 1);
            case "eidAdha"  -> findIslamicOccurrences(ethYear, 12, 10);
            case "moulid"   -> findIslamicOccurrences(ethYear,  3, 12);
            default -> List.of();
        };

        if (!occurrences.isEmpty()) {
            IslamicOccurrence occ = occurrences.get(0);
            List<String> tags = KenatConstants.MOVABLE_HOLIDAYS.getOrDefault(holidayKey, List.of());
            return new Holiday(holidayKey, tags, true, name, desc, occ.ethiopian(), occ.gregorian());
        }

        return null;
    }

    /** Convenience overload defaulting to Amharic. */
    public static Holiday getHoliday(String holidayKey, int ethYear) {
        return getHoliday(holidayKey, ethYear, "amharic");
    }

    /**
     * Returns all holidays that fall in the given Ethiopian month, sorted by day.
     *
     * @param ethYear  Ethiopian year
     * @param ethMonth Ethiopian month (1–13)
     * @param lang     language for names
     * @param filter   optional tag filter ({@code null} = no filter)
     * @return sorted list of holidays
     */
    public static List<Holiday> getHolidaysInMonth(int ethYear, int ethMonth,
                                                    String lang, List<String> filter) {
        List<Holiday> result = new ArrayList<>();

        // Fixed + movable Christian
        for (String key : KenatConstants.HOLIDAY_INFO.keySet()) {
            Holiday h = getHoliday(key, ethYear, lang);
            if (h != null && h.ethiopian().month() == ethMonth) {
                result.add(h);
            }
        }

        // Islamic — may occur twice per Ethiopian year
        addIslamicToList(result, ethYear, ethMonth, lang, "moulid",  3, 12);
        addIslamicToList(result, ethYear, ethMonth, lang, "eidFitr", 10, 1);
        addIslamicToList(result, ethYear, ethMonth, lang, "eidAdha", 12, 10);

        // Deduplicate by Ethiopian date
        result = deduplicate(result);

        if (filter != null && !filter.isEmpty()) {
            final List<String> f = filter;
            result = result.stream()
                    .filter(h -> h.tags().stream().anyMatch(f::contains))
                    .collect(Collectors.toList());
        }

        result.sort(Comparator.comparingInt(h -> h.ethiopian().day()));
        return result;
    }

    /** Convenience overload with no filter. */
    public static List<Holiday> getHolidaysInMonth(int ethYear, int ethMonth, String lang) {
        return getHolidaysInMonth(ethYear, ethMonth, lang, null);
    }

    /**
     * Returns all holidays in the given Ethiopian year, sorted by month then day.
     *
     * @param ethYear Ethiopian year
     * @param lang    language for names
     * @param filter  optional tag filter ({@code null} = no filter)
     * @return sorted list of holidays
     */
    public static List<Holiday> getHolidaysForYear(int ethYear, String lang, List<String> filter) {
        List<Holiday> result = new ArrayList<>();

        // Fixed holidays
        for (String key : FIXED.keySet()) {
            Holiday h = getHoliday(key, ethYear, lang);
            if (h != null) result.add(h);
        }

        // Movable Christian
        for (String key : KenatConstants.KEY_TO_TEWSAK_MAP.keySet()) {
            Holiday h = getHoliday(key, ethYear, lang);
            if (h != null) result.add(h);
        }

        // Islamic — all occurrences
        addAllIslamicToList(result, ethYear, lang, "moulid",  3, 12);
        addAllIslamicToList(result, ethYear, lang, "eidFitr", 10, 1);
        addAllIslamicToList(result, ethYear, lang, "eidAdha", 12, 10);

        if (filter != null && !filter.isEmpty()) {
            final List<String> f = filter;
            result = result.stream()
                    .filter(h -> h.tags().stream().anyMatch(f::contains))
                    .collect(Collectors.toList());
        }

        result.sort(Comparator
                .comparingInt((Holiday h) -> h.ethiopian().month())
                .thenComparingInt(h -> h.ethiopian().day()));
        return result;
    }

    /** Convenience overload with no filter. */
    public static List<Holiday> getHolidaysForYear(int ethYear, String lang) {
        return getHolidaysForYear(ethYear, lang, null);
    }

    // -----------------------------------------------------------------------
    // Islamic date conversion helpers
    // -----------------------------------------------------------------------

    /**
     * Simple arithmetic Hijri calendar approximation.
     * Converts a Hijri date to a Gregorian {@link LocalDate} using the
     * Kuwaiti algorithm (same algorithm used by most browser Intl implementations).
     *
     * @param hYear  Hijri year
     * @param hMonth Hijri month (1–12)
     * @param hDay   Hijri day (1–30)
     * @return Gregorian equivalent, or {@code null} if conversion fails
     */
    static LocalDate hijriToGregorian(int hYear, int hMonth, int hDay) {
        // Julian Day Number for the given Hijri date
        long jdn = (long) Math.ceil(29.5001 * (hMonth - 1))
                + (hYear - 1) * 354L
                + (long) Math.floor((11L * hYear + 3L) / 30L)
                + 1948440L - 385L + hDay - 1L;

        // JDN → Gregorian
        long z = jdn + 1;
        long a = (long) Math.floor((z - 1867216.25) / 36524.25);
        a = z + 1 + a - (long) Math.floor(a / 4.0);
        long b = a + 1524;
        long c = (long) Math.floor((b - 122.1) / 365.25);
        long d = (long) Math.floor(365.25 * c);
        long e = (long) Math.floor((b - d) / 30.6001);

        int day   = (int) (b - d - (long) Math.floor(30.6001 * e));
        int month = (int) (e < 14 ? e - 1 : e - 13);
        int year  = (int) (month > 2 ? c - 4716 : c - 4715);

        try {
            return LocalDate.of(year, month, day);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the Hijri year and month/day from a Gregorian date.
     * Needed to find the Hijri year that corresponds to an Ethiopian year.
     *
     * @param date Gregorian date
     * @return int[3]: {hYear, hMonth, hDay}
     */
    static int[] gregorianToHijri(LocalDate date) {
        long jdn = date.toEpochDay() + 2440588L; // LocalDate epoch → JDN
        long l = jdn - 1948440L + 10632L;
        long n = (long) Math.floor((l - 1.0) / 10631.0);
        l = l - 10631L * n + 354L;
        long j = (long) (Math.floor((10985.0 - l) / 5316.0)
                * Math.floor(50.0 * l / 17719.0))
                - (long) (Math.floor(l / 5670.0)
                * Math.floor(43.0 * l / 15238.0));
        l = l - (long) (Math.floor(30.0 - j) / 15.0)
                * (long) Math.floor(17719.0 * j / 50.0)
                + (long) (Math.floor(j / 16.0)
                * Math.floor(15238.0 * j / 43.0)) - 30L;
        long year  = 30L * n + (long) Math.floor(l / 365.25);
        long month = (long) Math.floor(l / 29.5) + 1;
        long day   = l - (long) Math.floor(29.5 * (month - 1));
        return new int[]{(int) year, (int) month, (int) day};
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private record IslamicOccurrence(EthiopianDate ethiopian, GregorianDate gregorian) {}

    private static List<IslamicOccurrence> findIslamicOccurrences(
            int ethYear, int hijriMonth, int hijriDay) {

        // Get Gregorian year range that covers the Ethiopian year
        GregorianDate ethStart = Conversions.toGC(ethYear, 1, 1);
        GregorianDate ethEnd   = Conversions.toGC(ethYear, 13,
                EthiopianCalendarUtils.getEthiopianDaysInMonth(ethYear, 13));

        Set<EthiopianDate> seen = new LinkedHashSet<>();
        List<IslamicOccurrence> result = new ArrayList<>();

        for (int gYear = ethStart.year(); gYear <= ethEnd.year(); gYear++) {
            // Get approximate Hijri year for this Gregorian year
            int[] hNow = gregorianToHijri(LocalDate.of(gYear, 1, 1));
            for (int hYear = hNow[0] - 1; hYear <= hNow[0] + 1; hYear++) {
                LocalDate greg = hijriToGregorian(hYear, hijriMonth, hijriDay);
                if (greg == null || greg.getYear() != gYear) continue;

                EthiopianDate eth;
                try {
                    eth = Conversions.toEC(greg);
                } catch (Exception e) {
                    continue;
                }
                if (eth.year() != ethYear) continue;
                if (!seen.contains(eth)) {
                    seen.add(eth);
                    result.add(new IslamicOccurrence(eth,
                            new GregorianDate(greg.getYear(), greg.getMonthValue(), greg.getDayOfMonth())));
                }
            }
        }
        return result;
    }

    private static void addIslamicToList(List<Holiday> list, int ethYear, int ethMonth,
                                          String lang, String key, int hMonth, int hDay) {
        List<IslamicOccurrence> occs = findIslamicOccurrences(ethYear, hMonth, hDay);
        KenatConstants.HolidayInfo info = KenatConstants.HOLIDAY_INFO.get(key);
        List<String> tags = KenatConstants.MOVABLE_HOLIDAYS.getOrDefault(key, List.of());
        for (IslamicOccurrence occ : occs) {
            if (occ.ethiopian().month() != ethMonth) continue;
            String name = info != null ? resolve(info.name(), lang) : key;
            String desc = info != null ? resolve(info.description(), lang) : "";
            Holiday h = new Holiday(key, tags, true, name, desc, occ.ethiopian(), occ.gregorian());
            // only add if not already present at same date
            if (list.stream().noneMatch(x -> x.ethiopian().equals(occ.ethiopian()) && x.key().equals(key))) {
                list.add(h);
            }
        }
    }

    private static void addAllIslamicToList(List<Holiday> list, int ethYear,
                                             String lang, String key, int hMonth, int hDay) {
        List<IslamicOccurrence> occs = findIslamicOccurrences(ethYear, hMonth, hDay);
        KenatConstants.HolidayInfo info = KenatConstants.HOLIDAY_INFO.get(key);
        List<String> tags = KenatConstants.MOVABLE_HOLIDAYS.getOrDefault(key, List.of());
        for (IslamicOccurrence occ : occs) {
            String name = info != null ? resolve(info.name(), lang) : key;
            String desc = info != null ? resolve(info.description(), lang) : "";
            list.add(new Holiday(key, tags, true, name, desc, occ.ethiopian(), occ.gregorian()));
        }
    }

    private static List<Holiday> deduplicate(List<Holiday> list) {
        Map<String, Holiday> seen = new LinkedHashMap<>();
        for (Holiday h : list) {
            String k = h.ethiopian().year() + "/" + h.ethiopian().month() + "/" + h.ethiopian().day() + ":" + h.key();
            seen.putIfAbsent(k, h);
        }
        return new ArrayList<>(seen.values());
    }

    /**
     * Finds the start and end dates of a specific Hijri month that overlaps with
     * the given Ethiopian year.
     * Mirrors {@code findHijriMonthRanges} in {@code holidays.ts}.
     *
     * @param ethYear    Ethiopian year
     * @param hijriMonth Hijri month (1–12), e.g. 9 = Ramadan
     * @return list of {@link DateRange} records (usually 1–2 entries)
     */
    public static List<DateRange> findHijriMonthRanges(int ethYear, int hijriMonth) {
        GregorianDate ethStart = Conversions.toGC(ethYear, 1, 1);
        GregorianDate ethEnd   = Conversions.toGC(ethYear, 13,
                EthiopianCalendarUtils.getEthiopianDaysInMonth(ethYear, 13));

        List<DateRange> ranges = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (int gYear = ethStart.year(); gYear <= ethEnd.year(); gYear++) {
            int[] hNow = gregorianToHijri(LocalDate.of(gYear, 1, 1));
            for (int hYear = hNow[0] - 1; hYear <= hNow[0] + 1; hYear++) {
                LocalDate startGrg = hijriToGregorian(hYear, hijriMonth, 1);
                if (startGrg == null) continue;

                int nextMonth = (hijriMonth == 12) ? 1 : hijriMonth + 1;
                int nextYear  = (hijriMonth == 12) ? hYear + 1 : hYear;
                LocalDate nextMonthFirst = hijriToGregorian(nextYear, nextMonth, 1);
                if (nextMonthFirst == null) continue;
                LocalDate endGrg = nextMonthFirst.minusDays(1);

                EthiopianDate startEC, endEC;
                try {
                    startEC = Conversions.toEC(startGrg);
                    endEC   = Conversions.toEC(endGrg);
                } catch (Exception e) {
                    continue;
                }

                if (startEC.year() != ethYear && endEC.year() != ethYear) continue;

                String key = startEC.year() + "/" + startEC.month() + "/" + startEC.day();
                if (!seen.contains(key)) {
                    seen.add(key);
                    ranges.add(new DateRange(startEC, endEC));
                }
            }
        }
        return ranges;
    }

    private static String resolve(LocalizedText text, String lang) {
        if ("english".equalsIgnoreCase(lang)) return text.english();
        return text.amharic();
    }
}
