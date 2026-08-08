package io.github.henaorth16.kenat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/fasting.test.ts}.
 */
class FastingTest {

    private static final int YEAR = 2016;

    @Nested
    @DisplayName("getFastingPeriod — Christian fasts for 2016 E.C.")
    class ChristianFasts {

        @Test
        void abiyTsome() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.ABIY_TSOME, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 7, 2), period.start());
            assertEquals(new EthiopianDate(2016, 8, 25), period.end());
        }

        @Test
        void tsomeHawaryatEndsOnHamle4() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.TSOME_HAWARYAT, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 11, 4), period.end());
        }

        @Test
        void ninevehLastsThreeDays() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.NINEVEH, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 6, 18), period.start());
            assertEquals(new EthiopianDate(2016, 6, 20), period.end());
        }

        @Test
        void tsomeNebiyatIsFixed() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.TSOME_NEBIYAT, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 3, 15), period.start());
            assertEquals(new EthiopianDate(2016, 4, 28), period.end());
        }

        @Test
        void filsetaIsFixed() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.FILSETA, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 12, 1), period.start());
            assertEquals(new EthiopianDate(2016, 12, 14), period.end());
        }
    }

    @Nested
    @DisplayName("getFastingPeriod — Muslim fasts")
    class MuslimFasts {

        @Test
        void ramadanStartsMegabit2In2016() {
            DateRange period = Fasting.getFastingPeriod(FastingKeys.RAMADAN, YEAR).orElseThrow();
            assertEquals(new EthiopianDate(2016, 7, 2), period.start());
        }
    }

    @Nested
    @DisplayName("Error and edge cases")
    class Edges {

        @Test
        void unknownFastKeyIsEmpty() {
            assertTrue(Fasting.getFastingPeriod("UNKNOWN_FAST_KEY", YEAR).isEmpty());
        }

        @Test
        void futureYearStillResolves() {
            Optional<DateRange> period = Fasting.getFastingPeriod(FastingKeys.ABIY_TSOME, 2020);
            assertTrue(period.isPresent());
            assertNotNull(period.get().start());
            assertNotNull(period.get().end());
        }
    }

    @Nested
    @DisplayName("getFastingInfo")
    class Info {

        @Test
        void abiyTsomeIsMultilingual() {
            Fasting.FastingInfoResult am =
                    Fasting.getFastingInfo(FastingKeys.ABIY_TSOME, YEAR, "amharic").orElseThrow();
            assertEquals(FastingKeys.ABIY_TSOME, am.key());
            assertEquals("ዐቢይ ጾም (ሁዳዴ)", am.name());
            assertNotNull(am.description());
            assertEquals(new EthiopianDate(2016, 7, 2), am.period().start());

            Fasting.FastingInfoResult en =
                    Fasting.getFastingInfo(FastingKeys.ABIY_TSOME, YEAR, "english").orElseThrow();
            assertTrue(en.name().toLowerCase().contains("great lent"));
            assertEquals(new EthiopianDate(2016, 8, 25), en.period().end());
        }

        @Test
        void nineveh() {
            Fasting.FastingInfoResult info =
                    Fasting.getFastingInfo(FastingKeys.NINEVEH, YEAR).orElseThrow();
            assertEquals("ጾመ ነነዌ", info.name());
            assertEquals(new EthiopianDate(2016, 6, 18), info.period().start());
            assertEquals(new EthiopianDate(2016, 6, 20), info.period().end());
        }

        @Test
        void ramadanIncludesTags() {
            Fasting.FastingInfoResult info =
                    Fasting.getFastingInfo(FastingKeys.RAMADAN, YEAR, "english").orElseThrow();
            assertEquals("Ramadan", info.name());
            assertTrue(info.tags().contains(HolidayTags.MUSLIM));
            assertEquals(new EthiopianDate(2016, 7, 2), info.period().start());
        }

        @Test
        void filseta() {
            Fasting.FastingInfoResult info =
                    Fasting.getFastingInfo(FastingKeys.FILSETA, YEAR, "amharic").orElseThrow();
            assertEquals(FastingKeys.FILSETA, info.key());
            assertEquals("ፍልሰታ", info.name());
            assertEquals(new EthiopianDate(2016, 12, 1), info.period().start());
            assertEquals(new EthiopianDate(2016, 12, 14), info.period().end());
        }

        @Test
        void tsomeDihenetHasNoContiguousPeriod() {
            Fasting.FastingInfoResult info =
                    Fasting.getFastingInfo(FastingKeys.TSOME_DIHENET, YEAR).orElseThrow();
            assertNull(info.period());
        }

        @Test
        void unknownKeyIsEmpty() {
            assertTrue(Fasting.getFastingInfo("NOPE", YEAR).isEmpty());
        }
    }

    @Nested
    @DisplayName("Orthodox weekly fasting (Tsome Dihnet)")
    class WeeklyFast {

        @Test
        void wednesdaysAndFridaysFastOutsidePentecostSeason() {
            int month = 3; // Hidar — well before Easter season
            int daysInMonth = EthiopianCalendarUtils.getEthiopianDaysInMonth(YEAR, month);
            List<Integer> days = Fasting.getFastingDays(FastingKeys.TSOME_DIHENET, YEAR, month);

            boolean foundWedOrFri = false;
            for (int day = 1; day <= daysInMonth; day++) {
                int wd = EthiopianCalendarUtils.getWeekday(new EthiopianDate(YEAR, month, day));
                if (wd == 3 || wd == 5) {
                    foundWedOrFri = true;
                    assertTrue(days.contains(day), "expected fasting on day " + day);
                }
            }
            assertTrue(foundWedOrFri);
        }

        @Test
        void noFastingBetweenEasterAndPentecost() {
            BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(YEAR);
            EthiopianDate easter = bh.movableFeasts().get("fasika").ethiopian();
            EthiopianDate pentecost = bh.movableFeasts().get("paraclete").ethiopian();

            EthiopianDate d = DayArithmetic.addDays(easter, 1);
            while (true) {
                int wd = EthiopianCalendarUtils.getWeekday(d);
                if (wd == 3 || wd == 5) {
                    List<Integer> days = Fasting.getFastingDays(FastingKeys.TSOME_DIHENET, d.year(), d.month());
                    assertFalse(days.contains(d.day()), "unexpected fasting day at " + d);
                }
                if (d.equals(pentecost)) break;
                d = DayArithmetic.addDays(d, 1);
            }
        }

        @Test
        void fastingResumesAfterPentecost() {
            BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(YEAR);
            EthiopianDate pentecost = bh.movableFeasts().get("paraclete").ethiopian();

            boolean found = false;
            for (int i = 1; i <= 21; i++) {
                EthiopianDate d = DayArithmetic.addDays(pentecost, i);
                int wd = EthiopianCalendarUtils.getWeekday(d);
                if (wd == 3 || wd == 5) {
                    List<Integer> days = Fasting.getFastingDays(FastingKeys.TSOME_DIHENET, d.year(), d.month());
                    assertTrue(days.contains(d.day()), "expected fasting to resume at " + d);
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Nested
    @DisplayName("getFastingDays for range fasts")
    class RangeFastDays {

        @Test
        void monthsOutsideThePeriodReturnNothing() {
            assertTrue(Fasting.getFastingDays(FastingKeys.FILSETA, YEAR, 1).isEmpty());
        }

        @Test
        void wholePeriodMonthReturnsTheIntersection() {
            List<Integer> days = Fasting.getFastingDays(FastingKeys.FILSETA, YEAR, 12);
            assertEquals(14, days.size());
            assertEquals(1, days.get(0));
            assertEquals(14, days.get(days.size() - 1));
        }

        @Test
        void middleMonthOfAMultiMonthFastIsFullyCovered() {
            // Tsome Nebiyat runs Hidar 15 → Tahsas 28, so Tahsas starts at day 1
            List<Integer> days = Fasting.getFastingDays(FastingKeys.TSOME_NEBIYAT, YEAR, 4);
            assertEquals(1, days.get(0));
            assertEquals(28, days.get(days.size() - 1));
        }

        @Test
        void unknownKeyReturnsEmpty() {
            assertTrue(Fasting.getFastingDays("NOPE", YEAR, 1).isEmpty());
        }
    }
}
