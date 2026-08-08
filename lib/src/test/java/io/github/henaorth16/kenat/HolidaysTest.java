package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidInputTypeException;
import io.github.henaorth16.kenat.exception.UnknownHolidayException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/holidays.test.ts}.
 */
class HolidaysTest {

    private static List<String> keysOf(List<Holiday> holidays) {
        return holidays.stream().map(Holiday::key).toList();
    }

    @Nested
    @DisplayName("getHolidaysInMonth")
    class InMonth {

        @Test
        void returnsFixedHolidaysForMeskerem() {
            List<String> keys = keysOf(Holidays.getHolidaysInMonth(2016, 1, "amharic"));
            assertTrue(keys.contains("enkutatash"));
            assertTrue(keys.contains("meskel"));
        }

        @Test
        void includesMovableChristianHolidays() {
            List<Holiday> holidays = Holidays.getHolidaysInMonth(2016, 8, "amharic");

            Holiday fasika = holidays.stream().filter(h -> h.key().equals("fasika")).findFirst().orElse(null);
            assertNotNull(fasika);
            assertEquals(27, fasika.ethiopian().day());

            Holiday siklet = holidays.stream().filter(h -> h.key().equals("siklet")).findFirst().orElse(null);
            assertNotNull(siklet);
            assertEquals(25, siklet.ethiopian().day());
        }

        @Test
        void resultsAreSortedByDay() {
            List<Holiday> holidays = Holidays.getHolidaysInMonth(2016, 1, "amharic");
            for (int i = 1; i < holidays.size(); i++) {
                assertTrue(holidays.get(i - 1).ethiopian().day() <= holidays.get(i).ethiopian().day());
            }
        }

        @Test
        void tagFilterNarrowsResults() {
            List<Holiday> christian =
                    Holidays.getHolidaysInMonth(2016, 1, "amharic", List.of(HolidayTags.CHRISTIAN));
            assertFalse(christian.isEmpty());
            assertTrue(christian.stream().allMatch(h -> h.hasTag(HolidayTags.CHRISTIAN)));
            assertFalse(keysOf(christian).contains("enkutatash")); // cultural + public only
        }
    }

    @Nested
    @DisplayName("getHolidaysForYear")
    class ForYear {

        @Test
        void coversEveryFixedHoliday() {
            List<String> keys = keysOf(Holidays.getHolidaysForYear(2016, "amharic"));
            assertTrue(keys.containsAll(List.of(
                    "enkutatash", "meskel", "beherbehereseb", "gena", "timket",
                    "martyrsDay", "adwa", "labour", "patriots")));
        }

        @Test
        void isSortedByMonthThenDay() {
            List<Holiday> holidays = Holidays.getHolidaysForYear(2016, "amharic");
            for (int i = 1; i < holidays.size(); i++) {
                EthiopianDate a = holidays.get(i - 1).ethiopian();
                EthiopianDate b = holidays.get(i).ethiopian();
                assertTrue(a.month() < b.month() || (a.month() == b.month() && a.day() <= b.day()));
            }
        }
    }

    @Nested
    @DisplayName("getMovableHoliday (Bahire Hasab)")
    class Movable {

        @Test
        void fasikaFor2012Through2016() {
            assertEquals(new EthiopianDate(2012, 8, 11), BahireHasab.getMovableHoliday("TINSAYE", 2012));
            assertEquals(new EthiopianDate(2013, 8, 24), BahireHasab.getMovableHoliday("TINSAYE", 2013));
            assertEquals(new EthiopianDate(2014, 8, 16), BahireHasab.getMovableHoliday("TINSAYE", 2014));
            assertEquals(new EthiopianDate(2015, 8, 8),  BahireHasab.getMovableHoliday("TINSAYE", 2015));
            assertEquals(new EthiopianDate(2016, 8, 27), BahireHasab.getMovableHoliday("TINSAYE", 2016));
        }

        @Test
        void sikletFor2012Through2016() {
            assertEquals(new EthiopianDate(2012, 8, 9),  BahireHasab.getMovableHoliday("SIKLET", 2012));
            assertEquals(new EthiopianDate(2013, 8, 22), BahireHasab.getMovableHoliday("SIKLET", 2013));
            assertEquals(new EthiopianDate(2014, 8, 14), BahireHasab.getMovableHoliday("SIKLET", 2014));
            assertEquals(new EthiopianDate(2015, 8, 6),  BahireHasab.getMovableHoliday("SIKLET", 2015));
            assertEquals(new EthiopianDate(2016, 8, 25), BahireHasab.getMovableHoliday("SIKLET", 2016));
        }
    }

    @Nested
    @DisplayName("Error handling")
    class Errors {

        @Test
        void outOfRangeMonthThrows() {
            assertThrows(InvalidInputTypeException.class,
                    () -> Holidays.getHolidaysInMonth(2016, 0, "amharic"));
            assertThrows(InvalidInputTypeException.class,
                    () -> Holidays.getHolidaysInMonth(2016, 14, "amharic"));
        }

        @Test
        void unknownMovableHolidayKeyThrows() {
            assertThrows(UnknownHolidayException.class,
                    () -> BahireHasab.getMovableHoliday("UNKNOWN_HOLIDAY", 2016));
        }

        @Test
        void unknownHolidayKeyReturnsNull() {
            assertNull(Holidays.getHoliday("not-a-holiday", 2016));
        }
    }

    /**
     * Islamic dates come from the JDK's Umm al-Qura {@code HijrahChronology}
     * rather than ICU's astronomical calendar, so Eid al-Fitr lands one day
     * later than in the TypeScript library. See {@code Holidays#hijriToGregorian}.
     */
    @Nested
    @DisplayName("Movable Muslim holidays")
    class Muslim {

        @Test
        void moulid2016() {
            Holiday holiday = Holidays.getHoliday("moulid", 2016);
            assertNotNull(holiday);
            assertEquals(new EthiopianDate(2016, 1, 16), holiday.ethiopian());
            assertEquals(new GregorianDate(2023, 9, 27), holiday.gregorian());
        }

        @Test
        void eidFitr2016() {
            Holiday holiday = Holidays.getHoliday("eidFitr", 2016);
            assertNotNull(holiday);
            assertEquals(new EthiopianDate(2016, 8, 2), holiday.ethiopian());
            assertEquals(new GregorianDate(2024, 4, 10), holiday.gregorian());
        }

        @Test
        void eidAdha2016() {
            Holiday holiday = Holidays.getHoliday("eidAdha", 2016);
            assertNotNull(holiday);
            assertEquals(new EthiopianDate(2016, 10, 9), holiday.ethiopian());
            assertEquals(new GregorianDate(2024, 6, 16), holiday.gregorian());
        }

        @Test
        void islamicHolidaysResolveAcrossAWideYearRange() {
            for (int year = 1990; year <= 2080; year++) {
                assertNotNull(Holidays.getHoliday("eidFitr", year), "eidFitr missing for " + year);
                assertNotNull(Holidays.getHoliday("eidAdha", year), "eidAdha missing for " + year);
                assertNotNull(Holidays.getHoliday("moulid", year),  "moulid missing for " + year);
            }
        }

        @Test
        void muslimHolidaysAppearInTheirMonthListing() {
            List<String> keys = keysOf(Holidays.getHolidaysInMonth(2016, 1, "amharic"));
            assertTrue(keys.contains("moulid"));
        }
    }

    @Nested
    @DisplayName("findHijriMonthRanges")
    class HijriRanges {

        @Test
        void ramadanRangeFor2016() {
            List<DateRange> ranges = Holidays.findHijriMonthRanges(2016, 9);
            assertFalse(ranges.isEmpty());
            assertEquals(new EthiopianDate(2016, 7, 2), ranges.get(0).start());
        }

        @Test
        void everyEthiopianYearHasAtLeastOneRamadanRange() {
            for (int year = 1990; year <= 2080; year++) {
                assertFalse(Holidays.findHijriMonthRanges(year, 9).isEmpty(),
                        "no Ramadan range for " + year);
            }
        }
    }
}
