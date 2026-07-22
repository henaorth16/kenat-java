package io.github.henaorth16.kenat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.henaorth16.kenat.EthiopianCalendarUtils.*;

/**
 * Fasting-period calculator for the Ethiopian Orthodox and Islamic calendars.
 * Mirrors {@code fasting.ts} from the TypeScript implementation.
 *
 * <p>All methods are static; the class is not instantiable.
 */
public final class Fasting {

    private Fasting() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns the start and end dates of a specific fasting period.
     *
     * @param fastKey       one of the constants in {@link FastingKeys}
     * @param ethiopianYear Ethiopian year
     * @return {@link DateRange} with start and end, or {@link Optional#empty()} if not found
     */
    public static Optional<DateRange> getFastingPeriod(String fastKey, int ethiopianYear) {
        BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(ethiopianYear);

        return switch (fastKey) {
            case FastingKeys.ABIY_TSOME -> {
                Holiday abiy   = bh.movableFeasts().get("abiyTsome");
                Holiday siklet = bh.movableFeasts().get("siklet");
                yield (abiy != null && siklet != null)
                        ? Optional.of(new DateRange(abiy.ethiopian(), siklet.ethiopian()))
                        : Optional.empty();
            }
            case FastingKeys.TSOME_HAWARYAT -> {
                Holiday hawaryat = bh.movableFeasts().get("tsomeHawaryat");
                yield hawaryat != null
                        ? Optional.of(new DateRange(hawaryat.ethiopian(),
                                new EthiopianDate(ethiopianYear, 11, 4)))
                        : Optional.empty();
            }
            case FastingKeys.NINEVEH -> {
                Holiday nineveh = bh.movableFeasts().get("nineveh");
                yield nineveh != null
                        ? Optional.of(new DateRange(nineveh.ethiopian(),
                                DayArithmetic.addDays(nineveh.ethiopian(), 2)))
                        : Optional.empty();
            }
            case FastingKeys.TSOME_NEBIYAT ->
                Optional.of(new DateRange(
                        new EthiopianDate(ethiopianYear, 3, 15),
                        new EthiopianDate(ethiopianYear, 4, 28)));
            case FastingKeys.FILSETA ->
                Optional.of(new DateRange(
                        new EthiopianDate(ethiopianYear, 12, 1),
                        new EthiopianDate(ethiopianYear, 12, 14)));
            case FastingKeys.RAMADAN -> {
                List<DateRange> ranges = Holidays.findHijriMonthRanges(ethiopianYear, 9);
                yield ranges.isEmpty() ? Optional.empty() : Optional.of(ranges.get(0));
            }
            default -> Optional.empty();
        };
    }

    /**
     * Returns full fasting information (name, description, period) for a fast key.
     *
     * @param fastKey       one of the constants in {@link FastingKeys}
     * @param ethiopianYear Ethiopian year
     * @param lang          {@code "amharic"} or {@code "english"}
     * @return {@link FastingInfoResult} or {@link Optional#empty()} if not found
     */
    public static Optional<FastingInfoResult> getFastingInfo(String fastKey, int ethiopianYear, String lang) {
        KenatConstants.FastingInfo info = KenatConstants.FASTING_INFO.get(fastKey);
        if (info == null) return Optional.empty();

        String name = "english".equalsIgnoreCase(lang) ? info.name().english() : info.name().amharic();
        String desc = "english".equalsIgnoreCase(lang) ? info.description().english() : info.description().amharic();

        // TSOME_DIHENET is weekly — no single contiguous period
        if (FastingKeys.TSOME_DIHENET.equals(fastKey)) {
            return Optional.of(new FastingInfoResult(fastKey, name, desc, info.tags(), null));
        }

        Optional<DateRange> period = getFastingPeriod(fastKey, ethiopianYear);
        return period.map(p -> new FastingInfoResult(fastKey, name, desc, info.tags(), p));
    }

    /** Convenience overload defaulting to Amharic. */
    public static Optional<FastingInfoResult> getFastingInfo(String fastKey, int ethiopianYear) {
        return getFastingInfo(fastKey, ethiopianYear, "amharic");
    }

    /**
     * Returns the day numbers within the given Ethiopian month that belong to
     * a fasting period.
     *
     * <p>For {@link FastingKeys#TSOME_DIHENET}: returns all Wednesdays and Fridays
     * except those in the 50-day Pentecost season after Easter.
     * For other fasts: returns the intersection of the fast period with the month.
     *
     * @param fastKey Ethiopian fast key
     * @param year    Ethiopian year
     * @param month   Ethiopian month (1–13)
     * @return list of 1-based day numbers in ascending order
     */
    public static List<Integer> getFastingDays(String fastKey, int year, int month) {
        int daysInMonth = getEthiopianDaysInMonth(year, month);

        if (FastingKeys.TSOME_DIHENET.equals(fastKey)) {
            BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(year);
            Holiday fasika    = bh.movableFeasts().get("fasika");
            Holiday paraclete = bh.movableFeasts().get("paraclete");

            List<Integer> out = new ArrayList<>();
            for (int d = 1; d <= daysInMonth; d++) {
                EthiopianDate date = new EthiopianDate(year, month, d);
                int weekday = getWeekday(date); // 0=Sun … 6=Sat; Wed=3, Fri=5
                if (weekday != 3 && weekday != 5) continue;

                // Check Pentecost exclusion window
                if (fasika != null && paraclete != null) {
                    int daysFromEaster   = DayArithmetic.diffInDays(date, fasika.ethiopian());
                    int daysFromParaclete = DayArithmetic.diffInDays(date, paraclete.ethiopian());
                    if (daysFromEaster >= 1 && daysFromParaclete <= 0) continue;
                }
                out.add(d);
            }
            return out;
        }

        Optional<DateRange> period = getFastingPeriod(fastKey, year);
        if (period.isEmpty()) return List.of();

        DateRange p = period.get();
        EthiopianDate start = p.start();
        EthiopianDate end   = p.end();

        boolean beforeStart = (year < start.year())
                || (year == start.year() && month < start.month());
        boolean afterEnd    = (year > end.year())
                || (year == end.year() && month > end.month());
        if (beforeStart || afterEnd) return List.of();

        int startDay = (year == start.year() && month == start.month()) ? start.day() : 1;
        int endDay   = (year == end.year()   && month == end.month())   ? end.day()   : daysInMonth;

        List<Integer> result = new ArrayList<>();
        for (int d = startDay; d <= endDay; d++) result.add(d);
        return result;
    }

    // -----------------------------------------------------------------------
    // Result type
    // -----------------------------------------------------------------------

    /**
     * Holds the full metadata for a fasting period result.
     *
     * @param key         fast key (from {@link FastingKeys})
     * @param name        localized name
     * @param description localized description
     * @param tags        classification tags
     * @param period      start/end range (null for TSOME_DIHENET)
     */
    public record FastingInfoResult(
            String key,
            String name,
            String description,
            List<String> tags,
            DateRange period
    ) {}
}
