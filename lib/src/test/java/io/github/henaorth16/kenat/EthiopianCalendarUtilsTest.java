package io.github.henaorth16.kenat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/utils.test.ts}.
 */
class EthiopianCalendarUtilsTest {

    @Nested
    @DisplayName("dayOfYear")
    class DayOfYear {

        @Test
        void nonLeapYear() {
            assertEquals(1,   EthiopianCalendarUtils.dayOfYear(2023, 1, 1));
            assertEquals(32,  EthiopianCalendarUtils.dayOfYear(2023, 2, 1));
            assertEquals(59,  EthiopianCalendarUtils.dayOfYear(2023, 2, 28));
            assertEquals(60,  EthiopianCalendarUtils.dayOfYear(2023, 3, 1));
            assertEquals(365, EthiopianCalendarUtils.dayOfYear(2023, 12, 31));
        }

        @Test
        void leapYear() {
            assertEquals(60,  EthiopianCalendarUtils.dayOfYear(2024, 2, 29));
            assertEquals(61,  EthiopianCalendarUtils.dayOfYear(2024, 3, 1));
            assertEquals(366, EthiopianCalendarUtils.dayOfYear(2024, 12, 31));
        }
    }

    @Nested
    @DisplayName("monthDayFromDayOfYear")
    class MonthDayFromDayOfYear {

        @Test
        void nonLeapYear() {
            assertArrayEquals(new int[]{1, 1},   EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 1));
            assertArrayEquals(new int[]{1, 31},  EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 31));
            assertArrayEquals(new int[]{2, 1},   EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 32));
            assertArrayEquals(new int[]{2, 28},  EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 59));
            assertArrayEquals(new int[]{3, 1},   EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 60));
            assertArrayEquals(new int[]{12, 31}, EthiopianCalendarUtils.monthDayFromDayOfYear(2023, 365));
        }

        @Test
        void leapYear() {
            assertArrayEquals(new int[]{2, 29},  EthiopianCalendarUtils.monthDayFromDayOfYear(2024, 60));
            assertArrayEquals(new int[]{3, 1},   EthiopianCalendarUtils.monthDayFromDayOfYear(2024, 61));
            assertArrayEquals(new int[]{12, 31}, EthiopianCalendarUtils.monthDayFromDayOfYear(2024, 366));
        }

        @Test
        void isTheInverseOfDayOfYear() {
            for (int year : new int[]{2023, 2024}) {
                int length = EthiopianCalendarUtils.isGregorianLeapYear(year) ? 366 : 365;
                for (int doy = 1; doy <= length; doy++) {
                    int[] md = EthiopianCalendarUtils.monthDayFromDayOfYear(year, doy);
                    assertEquals(doy, EthiopianCalendarUtils.dayOfYear(year, md[0], md[1]));
                }
            }
        }
    }

    @Nested
    @DisplayName("isGregorianLeapYear")
    class GregorianLeapYear {

        @Test
        void divisibleByFourButNotHundred() {
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(2024));
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(1996));
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(2008));
        }

        @Test
        void notDivisibleByFour() {
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(2023));
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(2019));
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(2101));
        }

        @Test
        void divisibleByHundredButNotFourHundred() {
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(1900));
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(2100));
            assertFalse(EthiopianCalendarUtils.isGregorianLeapYear(1800));
        }

        @Test
        void divisibleByFourHundred() {
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(2000));
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(1600));
            assertTrue(EthiopianCalendarUtils.isGregorianLeapYear(2400));
        }
    }

    @Nested
    @DisplayName("isEthiopianLeapYear")
    class EthiopianLeapYear {

        @Test
        void yearsWhereYearMod4Is3() {
            assertTrue(EthiopianCalendarUtils.isEthiopianLeapYear(2003));
            assertTrue(EthiopianCalendarUtils.isEthiopianLeapYear(2011));
            assertTrue(EthiopianCalendarUtils.isEthiopianLeapYear(2015));
            assertTrue(EthiopianCalendarUtils.isEthiopianLeapYear(2019));
        }

        @Test
        void otherYears() {
            for (int year : new int[]{2010, 2012, 2013, 2014, 2020}) {
                assertFalse(EthiopianCalendarUtils.isEthiopianLeapYear(year));
            }
        }
    }

    @Nested
    @DisplayName("getEthiopianDaysInMonth")
    class DaysInMonth {

        @Test
        void monthsOneThroughTwelveAlwaysHave30Days() {
            for (int year : new int[]{2010, 2011, 2012, 2013}) {
                for (int month = 1; month <= 12; month++) {
                    assertEquals(30, EthiopianCalendarUtils.getEthiopianDaysInMonth(year, month));
                }
            }
        }

        @Test
        void pagumeHas6DaysInALeapYear() {
            assertEquals(6, EthiopianCalendarUtils.getEthiopianDaysInMonth(2011, 13));
            assertEquals(6, EthiopianCalendarUtils.getEthiopianDaysInMonth(2015, 13));
            assertEquals(6, EthiopianCalendarUtils.getEthiopianDaysInMonth(2019, 13));
        }

        @Test
        void pagumeHas5DaysOtherwise() {
            for (int year : new int[]{2010, 2012, 2013, 2014, 2020}) {
                assertEquals(5, EthiopianCalendarUtils.getEthiopianDaysInMonth(year, 13));
            }
        }
    }

    @Nested
    @DisplayName("isValidEthiopianDate")
    class Validity {

        @Test
        void acceptsOrdinaryDates() {
            assertTrue(EthiopianCalendarUtils.isValidEthiopianDate(2016, 1, 1));
            assertTrue(EthiopianCalendarUtils.isValidEthiopianDate(2016, 12, 30));
        }

        @Test
        void rejectsOutOfRangeMonthsAndDays() {
            assertFalse(EthiopianCalendarUtils.isValidEthiopianDate(2016, 0, 1));
            assertFalse(EthiopianCalendarUtils.isValidEthiopianDate(2016, 14, 1));
            assertFalse(EthiopianCalendarUtils.isValidEthiopianDate(2016, 1, 0));
            assertFalse(EthiopianCalendarUtils.isValidEthiopianDate(2016, 1, 31));
        }

        @Test
        void appliesTheLeapYearRuleToPagume() {
            assertTrue(EthiopianCalendarUtils.isValidEthiopianDate(2015, 13, 6));
            assertFalse(EthiopianCalendarUtils.isValidEthiopianDate(2016, 13, 6));
        }
    }

    @Nested
    @DisplayName("New Year helpers")
    class NewYear {

        @Test
        void gregorianDateOfEthiopianNewYear() {
            // Meskerem 1 2016 = 12 September 2023 (2024 is a Gregorian leap year)
            assertArrayEquals(new int[]{2023, 9, 12},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(2016));
            assertArrayEquals(new int[]{2022, 9, 11},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(2015));
        }

        @Test
        void ethiopianNewYearForGregorianYear() {
            assertArrayEquals(new int[]{2023, 9, 12},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(2023));
            assertArrayEquals(new int[]{2024, 9, 11},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(2024));
            assertArrayEquals(new int[]{2025, 9, 11},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(2025));
        }

        @Test
        void newYearOutside1900To2099() {
            // The Ethiopian leap cycle has no century exception, so 1900 and 2100
            // (non-leap Gregorian years) shift Meskerem 1 by a day.
            assertArrayEquals(new int[]{1895, 9, 11},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(1888));
            assertArrayEquals(new int[]{1883, 9, 11},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(1876));
            assertArrayEquals(new int[]{1899, 9, 11},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(1892));
            assertArrayEquals(new int[]{1900, 9, 11},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(1893));
            assertArrayEquals(new int[]{2100, 9, 12},
                    EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(2093));
            assertArrayEquals(new int[]{1895, 9, 11},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(1895));
            assertArrayEquals(new int[]{1899, 9, 11},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(1899));
            assertArrayEquals(new int[]{2100, 9, 12},
                    EthiopianCalendarUtils.getEthiopianNewYearForGregorian(2100));
        }

        @Test
        void theTwoNewYearHelpersAgree() {
            for (int ethYear = 1990; ethYear <= 2080; ethYear++) {
                int[] fromEthiopian = EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(ethYear);
                int[] fromGregorian =
                        EthiopianCalendarUtils.getEthiopianNewYearForGregorian(fromEthiopian[0]);
                assertArrayEquals(fromEthiopian, fromGregorian,
                        "helpers disagree for Ethiopian year " + ethYear);
            }
        }

        @Test
        void newYearHelperAgreesWithTheConverter() {
            for (int ethYear = 1990; ethYear <= 2080; ethYear++) {
                int[] newYear = EthiopianCalendarUtils.getGregorianDateOfEthiopianNewYear(ethYear);
                assertEquals(new GregorianDate(newYear[0], newYear[1], newYear[2]),
                        Conversions.toGC(ethYear, 1, 1), "mismatch for Ethiopian year " + ethYear);
            }
        }
    }

    @Nested
    @DisplayName("getWeekday")
    class Weekday {

        @Test
        void matchesTheGregorianWeekday() {
            // Meskerem 1 2016 = Tuesday 12 September 2023
            assertEquals(2, EthiopianCalendarUtils.getWeekday(new EthiopianDate(2016, 1, 1)));
        }

        @Test
        void advancesByOneEachDay() {
            EthiopianDate date = new EthiopianDate(2016, 1, 1);
            int expected = EthiopianCalendarUtils.getWeekday(date);
            for (int i = 0; i < 40; i++) {
                assertEquals(expected, EthiopianCalendarUtils.getWeekday(date));
                date = DayArithmetic.addDays(date, 1);
                expected = (expected + 1) % 7;
            }
        }
    }
}
