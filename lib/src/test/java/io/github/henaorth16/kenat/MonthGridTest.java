package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidGridConfigException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers {@link MonthGrid}, the Java counterpart of {@code MonthGrid.ts}.
 *
 * <p>The TypeScript suite ({@code monthgrid.test.ts}) exercises a monkey-patched
 * mock rather than the real class, so these tests target the real behaviour
 * described by {@code MonthGrid.ts} instead of transcribing that mock.
 */
class MonthGridTest {

    private static int firstNonNullIndex(List<MonthGrid.MonthGridDay> days) {
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i) != null) return i;
        }
        return -1;
    }

    private static long dayCount(List<MonthGrid.MonthGridDay> days) {
        return days.stream().filter(java.util.Objects::nonNull).count();
    }

    @Nested
    @DisplayName("Structure")
    class Structure {

        @Test
        void headersAlwaysHaveSevenEntries() {
            assertEquals(7, MonthGrid.create(2016, 1).headers().size());
        }

        @Test
        void standardMonthHas30Days() {
            assertEquals(30, dayCount(MonthGrid.create(2016, 1).days()));
        }

        @Test
        void pagumeHas5DaysInACommonYearAnd6InALeapYear() {
            assertEquals(5, dayCount(MonthGrid.create(2016, 13).days()));
            assertEquals(6, dayCount(MonthGrid.create(2015, 13).days()));
        }

        @Test
        void leadingPaddingAlignsTheFirstDayToItsColumn() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(2016, 1);
            int offset = firstNonNullIndex(grid.days());
            int firstWeekday = grid.days().get(offset).weekday();
            assertEquals(Math.floorMod(firstWeekday - grid.config().weekStart(), 7), offset);
        }

        @Test
        void weekStartShiftsBothHeadersAndPadding() {
            MonthGrid.MonthGridResult sundayFirst = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 0, false, "english", null, null, false));
            MonthGrid.MonthGridResult wednesdayFirst = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 3, false, "english", null, null, false));

            assertEquals("Sunday", sundayFirst.headers().get(0));
            assertEquals("Wednesday", wednesdayFirst.headers().get(0));
            assertNotEquals(firstNonNullIndex(sundayFirst.days()), firstNonNullIndex(wednesdayFirst.days()));
        }

        @Test
        void localizesMonthAndWeekdayNames() {
            MonthGrid.MonthGridResult english = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "english", null, null, false));
            assertEquals("Meskerem", english.monthName());
            assertEquals("Monday", english.headers().get(0));

            MonthGrid.MonthGridResult amharic = MonthGrid.create(2016, 1);
            assertEquals("መስከረም", amharic.monthName());
            assertEquals("ሰኞ", amharic.headers().get(0));
        }

        @Test
        void geezNumeralsAreUsedWhenRequested() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, true, "amharic", null, null, false));
            assertEquals("፳፻፲፮", grid.year());

            MonthGrid.MonthGridDay first = grid.days().get(firstNonNullIndex(grid.days()));
            assertEquals("፩", first.ethiopian().day());
            assertEquals("መስከረም", first.ethiopian().month());
        }

        @Test
        void gregorianDatesTrackTheEthiopianOnes() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(2016, 1);
            MonthGrid.MonthGridDay first = grid.days().get(firstNonNullIndex(grid.days()));
            assertEquals(new GregorianDate(2023, 9, 12), first.gregorian());
        }
    }

    @Nested
    @DisplayName("Navigation")
    class Navigation {

        @Test
        void upAdvancesOneMonth() {
            MonthGrid.MonthGridResult next = MonthGrid.create(2016, 1).up();
            assertEquals(2, next.month());
            assertEquals(2016, next.numericYear());
        }

        @Test
        void upRollsPagumeIntoTheNextYear() {
            MonthGrid.MonthGridResult next = MonthGrid.create(2016, 13).up();
            assertEquals(1, next.month());
            assertEquals(2017, next.numericYear());
        }

        @Test
        void downStepsBackOneMonth() {
            MonthGrid.MonthGridResult prev = MonthGrid.create(2016, 2).down();
            assertEquals(1, prev.month());
            assertEquals(2016, prev.numericYear());
        }

        @Test
        void downRollsMeskeremIntoThePreviousYear() {
            MonthGrid.MonthGridResult prev = MonthGrid.create(2016, 1).down();
            assertEquals(13, prev.month());
            assertEquals(2015, prev.numericYear());
        }

        @Test
        void navigationPreservesConfiguration() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 3, true, "english", null, "public", false));
            MonthGrid.MonthGridResult next = grid.up();
            assertEquals(3, next.config().weekStart());
            assertTrue(next.config().useGeez());
            assertEquals("english", next.config().weekdayLang());
            assertEquals("public", next.config().mode());
            assertEquals("Wednesday", next.headers().get(0));
        }

        @Test
        void upThenDownReturnsToTheSameMonth() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(2016, 13);
            MonthGrid.MonthGridResult roundTrip = grid.up().down();
            assertEquals(grid.month(), roundTrip.month());
            assertEquals(grid.numericYear(), roundTrip.numericYear());
        }
    }

    @Nested
    @DisplayName("Holiday modes")
    class Modes {

        private static List<Holiday> holidaysOn(MonthGrid.MonthGridResult grid, int day) {
            return grid.days().stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(d -> Integer.parseInt(d.ethiopian().day()) == day)
                    .findFirst()
                    .map(MonthGrid.MonthGridDay::holidays)
                    .orElseThrow();
        }

        @Test
        void publicModeKeepsOnlyPublicHolidays() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "public", false));
            grid.days().stream().filter(java.util.Objects::nonNull)
                    .flatMap(d -> d.holidays().stream())
                    .forEach(h -> assertTrue(h.hasTag(HolidayTags.PUBLIC), h.key() + " is not public"));
        }

        @Test
        void muslimModeAddsJummahToEveryFriday() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "muslim", false));

            long fridays = grid.days().stream().filter(java.util.Objects::nonNull)
                    .filter(d -> d.weekday() == 5).count();
            long jummahs = grid.days().stream().filter(java.util.Objects::nonNull)
                    .flatMap(d -> d.holidays().stream())
                    .filter(h -> h.key().equals("jummah")).count();

            assertTrue(fridays > 0);
            assertEquals(fridays, jummahs);
        }

        @Test
        void jummahOnlyLandsOnFridays() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "muslim", false));
            grid.days().stream().filter(java.util.Objects::nonNull)
                    .filter(d -> d.holidays().stream().anyMatch(h -> h.key().equals("jummah")))
                    .forEach(d -> assertEquals(5, d.weekday()));
        }

        @Test
        void christianModeAttachesNigsDays() {
            // Ledeta Mariam is a nigs on the 1st of Ginbot (month 9)
            MonthGrid.MonthGridResult ginbot = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 9, 1, false, "amharic", null, "christian", false));
            List<Holiday> firstDay = holidaysOn(ginbot, 1);

            Holiday ledeta = firstDay.stream()
                    .filter(h -> h.key().equals("ledeta_mariam")).findFirst().orElse(null);
            assertNotNull(ledeta, "expected ledeta_mariam as a nigs in Ginbot");
            assertTrue(ledeta.hasTag(HolidayTags.NIGS));
            assertTrue(ledeta.hasTag(HolidayTags.CHRISTIAN));
            assertEquals("ልደታ ማርያም", ledeta.name());
            assertEquals(new EthiopianDate(2016, 9, 1), ledeta.ethiopian());
        }

        @Test
        void nigsDaysAreOmittedOutsideTheirMonth() {
            MonthGrid.MonthGridResult meskerem = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "christian", false));
            assertTrue(holidaysOn(meskerem, 1).stream()
                    .noneMatch(h -> h.key().equals("ledeta_mariam")));
        }

        @Test
        void showAllSaintsSurfacesNonNigsCommemorations() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "christian", true));
            List<Holiday> firstDay = holidaysOn(grid, 1);

            Holiday ledeta = firstDay.stream()
                    .filter(h -> h.key().equals("ledeta_mariam")).findFirst().orElse(null);
            assertNotNull(ledeta);
            assertTrue(ledeta.hasTag(HolidayTags.SAINT_DAY));
            assertFalse(ledeta.hasTag(HolidayTags.NIGS));
        }

        @Test
        void saintsAreNotAddedOutsideChristianMode() {
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 9, 1, false, "amharic", null, null, true));
            assertTrue(grid.days().stream().filter(java.util.Objects::nonNull)
                    .flatMap(d -> d.holidays().stream())
                    .noneMatch(h -> h.hasTag(HolidayTags.NIGS) || h.hasTag(HolidayTags.SAINT_DAY)));
        }

        @Test
        void saintsAreNotAttachedToNonExistentPagumeDays() {
            // 2016 is not a leap year: Pagume has 5 days, so day 6..30 entries must be dropped
            MonthGrid.MonthGridResult grid = MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 13, 1, false, "amharic", null, "christian", true));
            assertEquals(5, dayCount(grid.days()));
        }
    }

    @Nested
    @DisplayName("Configuration validation")
    class Validation {

        @Test
        void yearWithoutMonthIsRejected() {
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, null, 1, false, "amharic", null, null, false)));
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(null, 1, 1, false, "amharic", null, null, false)));
        }

        @Test
        void outOfRangeWeekStartIsRejected() {
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, -1, false, "amharic", null, null, false)));
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 7, false, "amharic", null, null, false)));
        }

        @Test
        void outOfRangeMonthIsRejected() {
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(2016, 0));
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(2016, 14));
        }

        @Test
        void unsupportedWeekdayLangIsRejected() {
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, "klingon", null, null, false)));
            assertThrows(InvalidGridConfigException.class, () -> MonthGrid.create(
                    new MonthGrid.MonthGridConfig(2016, 1, 1, false, null, null, null, false)));
        }

        @Test
        void defaultConfigFallsBackToToday() {
            EthiopianDate today = Conversions.toEC(java.time.LocalDate.now());
            MonthGrid.MonthGridResult grid = MonthGrid.create();
            assertEquals(today.month(), grid.month());
            assertEquals(today.year(), grid.numericYear());
        }

        @Test
        void isTodayFlagIsSetForTheCurrentDate() {
            EthiopianDate today = Conversions.toEC(java.time.LocalDate.now());
            MonthGrid.MonthGridResult grid = MonthGrid.create(today.year(), today.month());
            assertEquals(1, grid.days().stream().filter(java.util.Objects::nonNull)
                    .filter(MonthGrid.MonthGridDay::isToday).count());
        }
    }

    @Nested
    @DisplayName("Year calendar")
    class YearCalendar {

        @Test
        void producesThirteenMonths() {
            List<MonthGrid.MonthGridResult> year = Kenat.getYearCalendar(2016);
            assertEquals(13, year.size());
            for (int i = 0; i < 13; i++) {
                assertEquals(i + 1, year.get(i).month());
            }
        }

        @Test
        void appliesTheSuppliedConfigToEveryMonth() {
            List<MonthGrid.MonthGridResult> year = Kenat.getYearCalendar(2016,
                    new MonthGrid.MonthGridConfig(2016, 1, 0, false, "english", null, "public", false));
            assertTrue(year.stream().allMatch(m -> m.headers().get(0).equals("Sunday")));
        }
    }
}
