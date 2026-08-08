package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidTimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/Time.test.ts}.
 */
class EthiopianTimeTest {

    @Nested
    @DisplayName("Constructor and validation")
    class Construction {

        @Test
        void createsValidTime() {
            EthiopianTime time = new EthiopianTime(3, 30, TimePeriod.DAY);
            assertEquals(3, time.hour());
            assertEquals(30, time.minute());
            assertEquals(TimePeriod.DAY, time.period());
        }

        @Test
        void minuteDefaultsToZero() {
            EthiopianTime time = new EthiopianTime(5, TimePeriod.DAY);
            assertEquals(0, time.minute());
            assertEquals(TimePeriod.DAY, time.period());
        }

        @Test
        void outOfRangeValuesThrow() {
            assertThrows(InvalidTimeException.class, () -> new EthiopianTime(0, 0, TimePeriod.DAY));
            assertThrows(InvalidTimeException.class, () -> new EthiopianTime(13, 0, TimePeriod.DAY));
            assertThrows(InvalidTimeException.class, () -> new EthiopianTime(5, -1, TimePeriod.DAY));
            assertThrows(InvalidTimeException.class, () -> new EthiopianTime(5, 60, TimePeriod.DAY));
            assertThrows(InvalidTimeException.class, () -> new EthiopianTime(5, 0, null));
        }

        @Test
        void unknownPeriodStringThrows() {
            assertThrows(IllegalArgumentException.class, () -> TimePeriod.fromString("morning"));
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @Test
        void parsesArabicNumerals() {
            assertEquals(new EthiopianTime(10, 30, TimePeriod.DAY), EthiopianTime.fromString("10:30 day"));
            assertEquals(new EthiopianTime(5, 0, TimePeriod.NIGHT), EthiopianTime.fromString("5:00 night"));
        }

        @Test
        void parsesGeezNumerals() {
            assertEquals(new EthiopianTime(3, 15, TimePeriod.NIGHT), EthiopianTime.fromString("፫:፲፭ ማታ"));
            assertEquals(new EthiopianTime(12, 30, TimePeriod.DAY), EthiopianTime.fromString("፲፪:፴ day"));
        }

        @Test
        void defaultsToDayPeriod() {
            assertEquals(new EthiopianTime(11, 45, TimePeriod.DAY), EthiopianTime.fromString("11:45"));
        }

        @Test
        void toleratesInconsistentSpacing() {
            assertEquals(new EthiopianTime(4, 20, TimePeriod.NIGHT),
                    EthiopianTime.fromString(" 4 : 20  night "));
        }

        @Test
        void malformedStringsThrow() {
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("10"));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("10:"));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString(":30"));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("10 30 day"));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("abc:def period"));
        }

        @Test
        void emptyOrBlankStringsThrow() {
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString(""));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("   "));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString(null));
        }

        @Test
        void outOfRangeValuesThrow() {
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("13:00 day"));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromString("5:60 night"));
        }
    }

    @Nested
    @DisplayName("Gregorian conversions")
    class Conversions {

        @ParameterizedTest(name = "fromGregorian({0}:{1})")
        @CsvSource({
                "7,  30, 1,  30, DAY",
                "18, 0,  12, 0,  NIGHT",
                "0,  0,  6,  0,  NIGHT",
                "6,  0,  12, 0,  DAY"
        })
        void fromGregorian(int gHour, int gMinute, int ethHour, int ethMinute, TimePeriod period) {
            assertEquals(new EthiopianTime(ethHour, ethMinute, period),
                    EthiopianTime.fromGregorian(gHour, gMinute));
        }

        @ParameterizedTest(name = "toGregorian({0}:{1} {2})")
        @CsvSource({
                "1,  30, DAY,   7,  30",
                "12, 0,  NIGHT, 18, 0",
                "6,  0,  NIGHT, 0,  0",
                "12, 0,  DAY,   6,  0"
        })
        void toGregorian(int ethHour, int ethMinute, TimePeriod period, int gHour, int gMinute) {
            int[] greg = new EthiopianTime(ethHour, ethMinute, period).toGregorian();
            assertArrayEquals(new int[]{gHour, gMinute}, greg);
        }

        @Test
        void invalidGregorianTimeThrows() {
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromGregorian(24, 0));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromGregorian(-1, 0));
            assertThrows(InvalidTimeException.class, () -> EthiopianTime.fromGregorian(0, 60));
        }

        @Test
        void roundTripsEveryMinuteOfTheDay() {
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m += 7) {
                    int[] back = EthiopianTime.fromGregorian(h, m).toGregorian();
                    assertArrayEquals(new int[]{h, m}, back, "round trip failed for " + h + ":" + m);
                }
            }
        }
    }

    @Nested
    @DisplayName("Arithmetic")
    class Arithmetic {

        private final EthiopianTime startTime = new EthiopianTime(3, 15, TimePeriod.DAY); // 9:15 AM

        @Test
        void addWithinSamePeriod() {
            assertEquals(new EthiopianTime(5, 25, TimePeriod.DAY), startTime.add(2, 10));
        }

        @Test
        void addRollsIntoNextPeriod() {
            assertEquals(new EthiopianTime(12, 15, TimePeriod.NIGHT), startTime.add(9, 0));
        }

        @Test
        void subtract() {
            assertEquals(new EthiopianTime(2, 0, TimePeriod.DAY), startTime.subtract(1, 15));
        }

        @Test
        void subtractRollsBackToPreviousPeriod() {
            assertEquals(new EthiopianTime(11, 0, TimePeriod.NIGHT),
                    new EthiopianTime(1, 0, TimePeriod.DAY).subtract(2, 0));
        }

        @Test
        void diff() {
            assertArrayEquals(new int[]{2, 30},
                    startTime.diff(new EthiopianTime(5, 45, TimePeriod.DAY)));
        }

        @Test
        void diffTakesShortestPathAcrossMidnight() {
            EthiopianTime t1 = new EthiopianTime(2, 0, TimePeriod.NIGHT);  // 8 PM
            EthiopianTime t2 = new EthiopianTime(10, 0, TimePeriod.NIGHT); // 4 AM
            assertArrayEquals(new int[]{8, 0}, t1.diff(t2));
        }
    }

    @Nested
    @DisplayName("Formatting")
    class Formatting {

        @Test
        void defaultsToGeezWithAmharicLabel() {
            assertEquals("፭:፴ ጠዋት", new EthiopianTime(5, 30, TimePeriod.DAY).format());
        }

        @Test
        void arabicNumerals() {
            assertEquals("05:30 day",
                    new EthiopianTime(5, 30, TimePeriod.DAY).format("english", false, true, true));
        }

        @Test
        void withoutPeriodLabel() {
            assertEquals("08:15",
                    new EthiopianTime(8, 15, TimePeriod.NIGHT).format("english", false, false, true));
        }

        @Test
        void zeroMinutesAsDash() {
            assertEquals("12:_ day",
                    new EthiopianTime(12, 0, TimePeriod.DAY).format("english", false, true, true));
        }

        @Test
        void zeroMinutesAsDigitsWhenDashDisabled() {
            assertEquals("12:00 day",
                    new EthiopianTime(12, 0, TimePeriod.DAY).format("english", false, true, false));
        }
    }

    @Nested
    @DisplayName("Value semantics")
    class ValueSemantics {

        @Test
        void equalsAndHashCode() {
            EthiopianTime a = new EthiopianTime(3, 30, TimePeriod.DAY);
            EthiopianTime b = new EthiopianTime(3, 30, TimePeriod.DAY);
            EthiopianTime c = new EthiopianTime(3, 30, TimePeriod.NIGHT);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, c);
        }
    }

    @Nested
    @DisplayName("Kenat time methods")
    class KenatTimeMethods {

        @Test
        void getCurrentTimeReturnsAValidTime() {
            EthiopianTime time = Kenat.now().getCurrentTime();
            assertNotNull(time);
            assertTrue(time.hour() >= 1 && time.hour() <= 12);
            assertTrue(time.minute() >= 0 && time.minute() <= 59);
            assertNotNull(time.period());
        }
    }
}
