package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidDateFormatException;
import io.github.henaorth16.kenat.exception.InvalidEthiopianDateException;
import io.github.henaorth16.kenat.exception.UnrecognizedInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/Kenat.test.ts}.
 */
class KenatTest {

    @Nested
    @DisplayName("Construction and conversion")
    class Construction {

        @Test
        void defaultConstructorUsesToday() {
            LocalDate today = LocalDate.now();
            Kenat kenat = new Kenat();
            GregorianDate g = kenat.getGregorian();
            assertEquals(new GregorianDate(today.getYear(), today.getMonthValue(), today.getDayOfMonth()), g);
            assertNotNull(kenat.getEthiopian());
        }

        @Test
        void convertsSpecificEthiopianDate() {
            Kenat kenat = new Kenat("2016/9/15");
            assertEquals(new GregorianDate(2024, 5, 23), kenat.getGregorian());
            assertEquals(new EthiopianDate(2016, 9, 15), kenat.getEthiopian());
        }

        @Test
        void acceptsDashSeparatedStrings() {
            assertEquals(new EthiopianDate(2016, 9, 15), new Kenat("2016-9-15").getEthiopian());
        }

        @Test
        void acceptsLocalDate() {
            assertEquals(new EthiopianDate(2016, 1, 1), new Kenat(LocalDate.of(2023, 9, 12)).getEthiopian());
        }

        @Test
        void malformedStringThrows() {
            assertThrows(InvalidDateFormatException.class, () -> new Kenat("not-a-date"));
            assertThrows(InvalidDateFormatException.class, () -> new Kenat("2016/9"));
            assertThrows(InvalidDateFormatException.class, () -> new Kenat(""));
        }

        @Test
        void invalidEthiopianDateThrows() {
            assertThrows(InvalidEthiopianDateException.class, () -> new Kenat("2016/14/1"));
            // 2016 is not a leap year, so Pagume 6 does not exist
            assertThrows(InvalidEthiopianDateException.class, () -> new Kenat("2016/13/6"));
        }

        @Test
        void ofCoercesSupportedInputs() {
            assertEquals(new EthiopianDate(2016, 1, 1), Kenat.of("2016/1/1").getEthiopian());
            assertEquals(new EthiopianDate(2016, 1, 1), Kenat.of(new EthiopianDate(2016, 1, 1)).getEthiopian());
            assertEquals(new EthiopianDate(2016, 1, 1), Kenat.of(LocalDate.of(2023, 9, 12)).getEthiopian());
            assertEquals(new EthiopianDate(2016, 1, 1), Kenat.of(new GregorianDate(2023, 9, 12)).getEthiopian());

            Kenat existing = new Kenat("2016/1/1");
            assertSame(existing, Kenat.of(existing));
        }

        @Test
        void ofRejectsUnsupportedInput() {
            assertThrows(UnrecognizedInputException.class, () -> Kenat.of(42));
            assertThrows(UnrecognizedInputException.class, () -> Kenat.of(null));
        }
    }

    @Nested
    @DisplayName("Formatting")
    class Formatting {

        @Test
        void toStringReturnsEthiopianDateAndTime() {
            assertEquals("ግንቦት 15 2016 12:00 ጠዋት", new Kenat("2016/9/15").toString());
        }

        @Test
        void formatUsesRequestedLanguage() {
            Kenat kenat = new Kenat("2017/1/15");
            assertEquals("Meskerem 15 2017", kenat.format("english"));
            assertEquals("መስከረም 15 2017", kenat.format("amharic"));
        }

        @Test
        void formatInGeezAmharic() {
            assertEquals("መስከረም ፲፭ ፳፻፲፯", new Kenat("2017/1/15").formatInGeezAmharic());
        }

        @Test
        void formatShort() {
            assertEquals("2017/01/15", new Kenat("2017/1/15").formatShort());
        }

        @Test
        void formatWithWeekday() {
            // Meskerem 1 2016 = 12 September 2023, a Tuesday
            assertEquals("ማክሰኞ, መስከረም 1 2016", new Kenat("2016/1/1").formatWithWeekday("amharic", false));
            assertEquals("Tuesday, Meskerem 1 2016", new Kenat("2016/1/1").formatWithWeekday("english", false));
        }
    }

    @Nested
    @DisplayName("Comparison helpers")
    class Comparison {

        private final Kenat date1 = new Kenat("2016/8/15");
        private final Kenat date2 = new Kenat("2016/8/20");
        private final Kenat date3 = new Kenat("2016/8/15");

        @Test
        void isBefore() {
            assertTrue(date1.isBefore(date2));
            assertFalse(date2.isBefore(date1));
            assertFalse(date1.isBefore(date3));
        }

        @Test
        void isAfter() {
            assertTrue(date2.isAfter(date1));
            assertFalse(date1.isAfter(date2));
            assertFalse(date1.isAfter(date3));
        }

        @Test
        void isSameDay() {
            assertTrue(date1.isSameDay(date3));
            assertFalse(date1.isSameDay(date2));
        }

        @Test
        void isSameMonth() {
            assertTrue(new Kenat("2017/5/1").isSameMonth(new Kenat("2017/5/30")));
            assertFalse(new Kenat("2017/5/15").isSameMonth(new Kenat("2017/6/15")));
            assertFalse(new Kenat("2016/5/15").isSameMonth(new Kenat("2017/5/15")));
        }

        @Test
        void isSameYear() {
            assertTrue(new Kenat("2017/1/1").isSameYear(new Kenat("2017/12/30")));
            assertFalse(new Kenat("2016/5/15").isSameYear(new Kenat("2017/5/15")));
        }
    }

    @Nested
    @DisplayName("Period boundaries and calendar queries")
    class Boundaries {

        private final Kenat date1 = new Kenat("2016/8/15");

        @Test
        void startOfMonth() {
            assertEquals(new EthiopianDate(2016, 8, 1), date1.startOfMonth().getEthiopian());
        }

        @Test
        void endOfMonthForStandardMonth() {
            assertEquals(new EthiopianDate(2016, 8, 30), date1.endOfMonth().getEthiopian());
        }

        @Test
        void endOfMonthForPagumeInLeapYear() {
            assertEquals(new EthiopianDate(2015, 13, 6), new Kenat("2015/13/1").endOfMonth().getEthiopian());
        }

        @Test
        void endOfYearIsPagume6InLeapYears() {
            assertEquals(new EthiopianDate(2015, 13, 6), new Kenat("2015/5/10").endOf("year").getEthiopian());
        }

        @Test
        void endOfYearIsPagume5InNonLeapYears() {
            assertEquals(new EthiopianDate(2016, 13, 5), new Kenat("2016/5/10").endOf("year").getEthiopian());
        }

        @Test
        void startOfYear() {
            assertEquals(new EthiopianDate(2016, 1, 1), new Kenat("2016/5/10").startOf("year").getEthiopian());
        }

        @Test
        void isLeapYear() {
            assertTrue(new Kenat("2015/1/1").isLeapYear());
            assertFalse(new Kenat("2016/1/1").isLeapYear());
        }

        @Test
        void weekday() {
            // Ginbot 15 2016 = 23 May 2024, a Thursday (index 4)
            assertEquals(4, new Kenat("2016/9/15").weekday());
        }
    }

    @Nested
    @DisplayName("Serialization")
    class Serialization {

        @Test
        void toJsonReturnsPlainMap() {
            Map<String, Object> json = new Kenat("2017/1/15").toJSON();
            assertEquals(Map.of("year", 2017, "month", 1, "day", 15), json.get("ethiopian"));
            assertInstanceOf(Map.class, json.get("gregorian"));
            assertNotNull(json.get("time"));

            @SuppressWarnings("unchecked")
            Map<String, Object> time = (Map<String, Object>) json.get("time");
            assertTrue(time.containsKey("hour"));
            assertTrue(time.containsKey("minute"));
            assertTrue(time.containsKey("period"));
        }

        @Test
        void toJsonIsUnmodifiable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> new Kenat("2017/1/15").toJSON().put("x", "y"));
        }
    }

    /**
     * Regression tests for kenat issue #34 — "Ability To Give End Users To Switch
     * Their Calendar Preferences".
     */
    @Nested
    @DisplayName("Calendar preference option")
    class CalendarPreference {

        // Meskerem 1, 2016 -> 12 September 2023
        private final Kenat kenat = new Kenat("2016/1/1");

        @Test
        void getDateDefaultsToEthiopian() {
            assertEquals(new EthiopianDate(2016, 1, 1), kenat.getDate("ethiopian"));
        }

        @Test
        void getDateReturnsGregorianWhenRequested() {
            assertEquals(new GregorianDate(2023, 9, 12), kenat.getDate("gregorian"));
        }

        @Test
        void formatDefaultsToEthiopian() {
            assertEquals("መስከረም 1 2016", kenat.format());
        }

        @Test
        void formatRendersGregorian() {
            assertEquals("September 12, 2023",
                    kenat.format("gregorian", "amharic", false, false, false));
        }

        @Test
        void formatSupportsGregorianWeekday() {
            assertEquals("ማክሰኞ, September 12, 2023",
                    kenat.format("gregorian", "amharic", true, false, false));
            assertEquals("Tuesday, September 12, 2023",
                    kenat.format("gregorian", "english", true, false, false));
        }

        @Test
        void formatSupportsGregorianTime() {
            Kenat timed = new Kenat(new EthiopianDate(2016, 1, 1),
                    new EthiopianTime(8, 30, TimePeriod.DAY));
            assertEquals("September 12, 2023 08:30 ጠዋት",
                    timed.format("gregorian", "amharic", false, false, true));
        }

        @Test
        void toStringDefaultsToEthiopian() {
            assertTrue(kenat.toString().contains("መስከረም 1 2016"));
        }

        @Test
        void toStringRendersGregorian() {
            assertTrue(kenat.toString("gregorian").contains("September 12, 2023"));
        }

        @Test
        void isoStringDefaultsToEthiopian() {
            assertEquals("2016-01-01T12:00", kenat.toISOString());
        }

        @Test
        void isoStringAppendsNonStandardSuffixForNight() {
            Kenat night = new Kenat(new EthiopianDate(2016, 1, 1),
                    new EthiopianTime(8, 15, TimePeriod.NIGHT));
            assertEquals("2016-01-01T08:15+12h", night.toISOString());
        }

        @Test
        void isoStringConvertsTimeForGregorian() {
            // 12:00 Ethiopian day time is 06:00 Gregorian
            assertEquals("2023-09-12T06:00", kenat.toISOString("gregorian"));
        }

        @Test
        void isoStringForGregorianNightHasNoSuffix() {
            Kenat night = new Kenat(new EthiopianDate(2016, 1, 1),
                    new EthiopianTime(8, 15, TimePeriod.NIGHT));
            String iso = night.toISOString("gregorian");
            assertEquals("2023-09-12T02:15", iso);
            assertTrue(iso.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"));
            assertDoesNotThrow(() -> LocalDate.parse(iso.substring(0, 10)));
        }
    }

    @Nested
    @DisplayName("isHoliday")
    class IsHoliday {

        @Test
        void newYearsDayIsAHoliday() {
            List<Holiday> holidays = new Kenat("2016/1/1").isHoliday();
            assertTrue(holidays.stream().anyMatch(h -> h.key().equals("enkutatash")));
        }

        @Test
        void ordinaryDayIsNotAHoliday() {
            assertTrue(new Kenat("2016/1/5").isHoliday().isEmpty());
        }
    }

    @Nested
    @DisplayName("generateDateRange")
    class DateRangeGeneration {

        @Test
        void inclusiveOfBothEnds() {
            List<Kenat> range = Kenat.generateDateRange(new Kenat("2016/1/1"), new Kenat("2016/1/5"));
            assertEquals(5, range.size());
            assertEquals(new EthiopianDate(2016, 1, 1), range.get(0).getEthiopian());
            assertEquals(new EthiopianDate(2016, 1, 5), range.get(4).getEthiopian());
        }

        @Test
        void singleDayRange() {
            assertEquals(1, Kenat.generateDateRange(new Kenat("2016/1/1"), new Kenat("2016/1/1")).size());
        }

        @Test
        void reversedRangeIsEmpty() {
            assertTrue(Kenat.generateDateRange(new Kenat("2016/1/5"), new Kenat("2016/1/1")).isEmpty());
        }
    }
}
