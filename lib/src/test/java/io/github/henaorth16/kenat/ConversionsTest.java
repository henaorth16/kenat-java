package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidGregorianDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/conversions.test.ts}.
 *
 * <p>These are regression fixtures — the expected dates must match the
 * TypeScript library exactly.
 */
class ConversionsTest {

    @Nested
    @DisplayName("Ethiopian to Gregorian conversion")
    class EthiopianToGregorian {

        @Test
        void meskerem14_2017() {
            assertEquals(new GregorianDate(2025, 5, 22), Conversions.toGC(2017, 9, 14));
        }

        @Test
        void pagume5_2016() {
            assertEquals(new GregorianDate(2024, 9, 10), Conversions.toGC(2016, 13, 5));
        }

        @Test
        void pagume6_2011_leapYear() {
            assertEquals(new GregorianDate(2019, 9, 11), Conversions.toGC(2011, 13, 6));
        }

        @Test
        void pagume6_2019_leapYear() {
            assertEquals(new GregorianDate(2027, 9, 11), Conversions.toGC(2019, 13, 6));
        }

        @Test
        void miazia27_2016() {
            assertEquals(new GregorianDate(2024, 5, 5), Conversions.toGC(2016, 8, 27));
        }

        @Test
        void meskerem1_2016() {
            assertEquals(new GregorianDate(2023, 9, 12), Conversions.toGC(2016, 1, 1));
        }

        @Test
        void tahsas30_2015() {
            assertEquals(new GregorianDate(2023, 1, 8), Conversions.toGC(2015, 4, 30));
        }

        @Test
        void pagume1_2011() {
            assertEquals(new GregorianDate(2019, 9, 6), Conversions.toGC(2011, 13, 1));
        }

        @Test
        void meskerem1_1964_baseDate() {
            assertEquals(new GregorianDate(1971, 9, 12), Conversions.toGC(1964, 1, 1));
        }

        @Test
        void pagume6_2007_leapPagume() {
            assertEquals(new GregorianDate(2015, 9, 11), Conversions.toGC(2007, 13, 6));
        }

        @Test
        void pagume5_2006_nonLeapPagume() {
            assertEquals(new GregorianDate(2014, 9, 10), Conversions.toGC(2006, 13, 5));
        }

        @Test
        void endOfEthiopianYear2015() {
            assertEquals(new GregorianDate(2023, 9, 10), Conversions.toGC(2015, 13, 5));
        }

        @Test
        void startOfEthiopianYear2015() {
            assertEquals(new GregorianDate(2022, 9, 11), Conversions.toGC(2015, 1, 1));
        }

        @Test
        void acceptsEthiopianDateRecord() {
            assertEquals(new GregorianDate(2023, 9, 12), Conversions.toGC(new EthiopianDate(2016, 1, 1)));
        }

        @Test
        void toGCDateReturnsLocalDate() {
            assertEquals(LocalDate.of(2023, 9, 12), Conversions.toGCDate(2016, 1, 1));
        }
    }

    @Nested
    @DisplayName("Gregorian to Ethiopian conversion")
    class GregorianToEthiopian {

        @Test
        void may22_2025() {
            assertEquals(new EthiopianDate(2017, 9, 14), Conversions.toEC(2025, 5, 22));
        }

        @Test
        void feb29_2020_leapYear() {
            assertEquals(new EthiopianDate(2012, 6, 21), Conversions.toEC(2020, 2, 29));
        }

        @Test
        void may5_2024() {
            assertEquals(new EthiopianDate(2016, 8, 27), Conversions.toEC(2024, 5, 5));
        }

        @Test
        void sep10_2024() {
            assertEquals(new EthiopianDate(2016, 13, 5), Conversions.toEC(2024, 9, 10));
        }

        @Test
        void sep11_2019_leapPagume() {
            assertEquals(new EthiopianDate(2011, 13, 6), Conversions.toEC(2019, 9, 11));
        }

        @Test
        void sep11_2027_leapPagume() {
            assertEquals(new EthiopianDate(2019, 13, 6), Conversions.toEC(2027, 9, 11));
        }

        @Test
        void jan1_2000() {
            assertEquals(new EthiopianDate(1992, 4, 22), Conversions.toEC(2000, 1, 1));
        }

        @Test
        void sep12_1971_baseDate() {
            assertEquals(new EthiopianDate(1964, 1, 1), Conversions.toEC(1971, 9, 12));
        }

        @Test
        void endOfGregorianLeapYear2020() {
            assertEquals(new EthiopianDate(2013, 4, 22), Conversions.toEC(2020, 12, 31));
        }

        @Test
        void startOfGregorianLeapYear2020() {
            assertEquals(new EthiopianDate(2012, 4, 22), Conversions.toEC(2020, 1, 1));
        }

        @Test
        void lastDayOfEthiopianYear() {
            assertEquals(new EthiopianDate(2015, 13, 5), Conversions.toEC(2023, 9, 10));
        }

        @Test
        void leapPagume_sep11_2023() {
            assertEquals(new EthiopianDate(2015, 13, 6), Conversions.toEC(2023, 9, 11));
        }

        @Test
        void upperBoundStaysValid() {
            EthiopianDate result = Conversions.toEC(2100, 12, 31);
            assertTrue(result.year() >= 2092);
            assertTrue(result.month() >= 4);
            assertTrue(result.day() >= 20);
        }

        @Test
        void lowerBoundStaysValid() {
            EthiopianDate result = Conversions.toEC(1900, 1, 1);
            assertTrue(result.year() <= 1892);
            assertTrue(result.month() >= 4);
            assertTrue(result.day() >= 22);
        }

        @Test
        void outOfRangeYearThrows() {
            assertThrows(InvalidGregorianDateException.class, () -> Conversions.toEC(1800, 1, 1));
            assertThrows(InvalidGregorianDateException.class, () -> Conversions.toEC(2200, 1, 1));
        }

        @Test
        void invalidCalendarDateThrows() {
            assertThrows(InvalidGregorianDateException.class, () -> Conversions.toEC(2023, 2, 30));
            assertThrows(InvalidGregorianDateException.class, () -> Conversions.toEC(2023, 13, 1));
        }

        @Test
        void acceptsLocalDate() {
            assertEquals(new EthiopianDate(2016, 1, 1), Conversions.toEC(LocalDate.of(2023, 9, 12)));
        }
    }

    @Nested
    @DisplayName("Round trip")
    class RoundTrip {

        @Test
        void everyDayOfEthiopianYear2016RoundTrips() {
            for (int month = 1; month <= 13; month++) {
                int days = EthiopianCalendarUtils.getEthiopianDaysInMonth(2016, month);
                for (int day = 1; day <= days; day++) {
                    EthiopianDate original = new EthiopianDate(2016, month, day);
                    assertEquals(original, Conversions.toEC(Conversions.toGCDate(2016, month, day)),
                            "round trip failed for " + original);
                }
            }
        }
    }
}
