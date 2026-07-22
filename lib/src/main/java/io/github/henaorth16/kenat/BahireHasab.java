package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.UnknownHolidayException;

import java.util.*;

/**
 * Bahire Hasab — Ethiopian movable-feast calculator.
 * Mirrors {@code bahireHasab.ts} from the TypeScript implementation.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class BahireHasab {

    private BahireHasab() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Calculates the full Bahire Hasab result for the given Ethiopian year,
     * including all movable feasts.
     *
     * @param ethiopianYear Ethiopian year
     * @param lang          {@code "amharic"} or {@code "english"}
     * @return {@link BahireHasabResult} with all calculated values
     */
    public static BahireHasabResult getBahireHasab(int ethiopianYear, String lang) {
        Base base = calculateBase(ethiopianYear);

        int evangelistRemainder = base.ameteAlem() % 4;
        String evangelistName = Optional.ofNullable(
                "english".equalsIgnoreCase(lang)
                        ? KenatConstants.EVANGELIST_NAMES_ENGLISH
                        : KenatConstants.EVANGELIST_NAMES_AMHARIC
        ).map(m -> m.get(evangelistRemainder)).orElse("?");

        int tinteQemer = (base.ameteAlem() + base.meteneRabiet()) % 7;
        int weekdayIndex = (tinteQemer + 1) % 7;
        List<String> wdays = "english".equalsIgnoreCase(lang)
                ? KenatConstants.DAYS_OF_WEEK_ENGLISH
                : KenatConstants.DAYS_OF_WEEK_AMHARIC;
        String newYearWeekday = wdays.get(weekdayIndex);

        // Build movable feasts
        Map<String, String> tewsakToKey = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : KenatConstants.KEY_TO_TEWSAK_MAP.entrySet()) {
            tewsakToKey.put(e.getValue(), e.getKey());
        }

        Map<String, Holiday> movableFeasts = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : KenatConstants.MOVABLE_HOLIDAY_TEWSAK.entrySet()) {
            String tewsakKey = e.getKey();
            int offset = e.getValue();
            String holidayKey = tewsakToKey.get(tewsakKey);
            if (holidayKey == null) continue;

            EthiopianDate date = DayArithmetic.addDays(base.ninevehDate(), offset);
            KenatConstants.HolidayInfo info = KenatConstants.HOLIDAY_INFO.get(holidayKey);
            List<String> tags = KenatConstants.MOVABLE_HOLIDAYS.getOrDefault(holidayKey, List.of());
            String name = info != null
                    ? (lang.equalsIgnoreCase("english") ? info.name().english() : info.name().amharic())
                    : holidayKey;
            String desc = info != null
                    ? (lang.equalsIgnoreCase("english") ? info.description().english() : info.description().amharic())
                    : "";
            GregorianDate greg = Conversions.toGC(date);

            movableFeasts.put(holidayKey, new Holiday(holidayKey, tags, true, name, desc, date, greg));
        }

        // bealeMetqi weekday in English
        int bealeWeekdayIdx = EthiopianCalendarUtils.getWeekday(base.bealeMetqiDate());
        String bealeMetqiWeekday = KenatConstants.DAYS_OF_WEEK_ENGLISH.get(bealeWeekdayIdx);

        return new BahireHasabResult(
                base.ameteAlem(),
                base.meteneRabiet(),
                evangelistRemainder,
                evangelistName,
                tinteQemer,
                newYearWeekday,
                base.medeb(),
                base.wenber(),
                base.abektie(),
                base.metqi(),
                base.bealeMetqiDate(),
                bealeMetqiWeekday,
                base.mebajaHamer(),
                base.ninevehDate(),
                Collections.unmodifiableMap(movableFeasts)
        );
    }

    /** Convenience overload defaulting to Amharic. */
    public static BahireHasabResult getBahireHasab(int ethiopianYear) {
        return getBahireHasab(ethiopianYear, "amharic");
    }

    /**
     * Returns the Ethiopian date of a specific movable holiday.
     *
     * @param holidayKey uppercase tewsak key, e.g. {@code "TINSAYE"}
     * @param ethiopianYear Ethiopian year
     * @return the Ethiopian date of the holiday
     * @throws UnknownHolidayException if the key is unrecognised
     */
    public static EthiopianDate getMovableHoliday(String holidayKey, int ethiopianYear) {
        Integer tewsak = KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get(holidayKey);
        if (tewsak == null) throw new UnknownHolidayException(holidayKey);
        Base base = calculateBase(ethiopianYear);
        return DayArithmetic.addDays(base.ninevehDate(), tewsak);
    }

    // -----------------------------------------------------------------------
    // Core calculation
    // -----------------------------------------------------------------------

    /**
     * Calculates all base Bahire Hasab values for the given Ethiopian year.
     * This is the single source of truth for the algorithm.
     */
    static Base calculateBase(int ethiopianYear) {
        int ameteAlem     = 5500 + ethiopianYear;
        int meteneRabiet  = ameteAlem / 4;
        int medeb         = ameteAlem % 19;
        int wenber        = (medeb == 0) ? 18 : medeb - 1;
        int abektie       = (wenber * 11) % 30;
        int rawMetqi      = (wenber * 19) % 30;
        int metqi         = (rawMetqi == 0) ? 30 : rawMetqi;

        int bealeMetqiMonth = (metqi > 14) ? 1 : 2;
        int bealeMetqiDay   = metqi;
        EthiopianDate bealeMetqiDate = new EthiopianDate(ethiopianYear, bealeMetqiMonth, bealeMetqiDay);

        String bealeWeekday = KenatConstants.DAYS_OF_WEEK_ENGLISH.get(
                EthiopianCalendarUtils.getWeekday(bealeMetqiDate));
        int tewsak = KenatConstants.TEWSAK_MAP.get(bealeWeekday);

        int mebajaHamerSum = bealeMetqiDay + tewsak;
        int mebajaHamer    = (mebajaHamerSum > 30) ? mebajaHamerSum % 30 : mebajaHamerSum;

        int ninevehMonth = (metqi > 14) ? 5 : 6;
        if (mebajaHamerSum > 30) ninevehMonth++;
        EthiopianDate ninevehDate = new EthiopianDate(ethiopianYear, ninevehMonth, mebajaHamer);

        return new Base(ameteAlem, meteneRabiet, medeb, wenber, abektie, metqi,
                bealeMetqiDate, mebajaHamer, ninevehDate);
    }

    // -----------------------------------------------------------------------
    // Internal value record
    // -----------------------------------------------------------------------

    record Base(
            int ameteAlem,
            int meteneRabiet,
            int medeb,
            int wenber,
            int abektie,
            int metqi,
            EthiopianDate bealeMetqiDate,
            int mebajaHamer,
            EthiopianDate ninevehDate
    ) {}

    // -----------------------------------------------------------------------
    // Result type
    // -----------------------------------------------------------------------

    /**
     * Full Bahire Hasab result for one Ethiopian year.
     */
    public record BahireHasabResult(
            int ameteAlem,
            int meteneRabiet,
            int evangelistRemainder,
            String evangelistName,
            int tinteQemer,
            String newYearWeekday,
            int medeb,
            int wenber,
            int abektie,
            int metqi,
            EthiopianDate bealeMetqiDate,
            String bealeMetqiWeekday,
            int mebajaHamer,
            EthiopianDate ninevehDate,
            Map<String, Holiday> movableFeasts
    ) {
        /** Convenience accessor — returns a movable feast by camelCase key. */
        public Optional<Holiday> feast(String key) {
            return Optional.ofNullable(movableFeasts.get(key));
        }
    }
}
