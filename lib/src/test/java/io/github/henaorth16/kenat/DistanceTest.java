package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.UnknownHolidayException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the distance helpers on {@link Kenat} — the Java counterpart of
 * {@code distanceTo}, {@code distanceFromToday}, {@code distanceToHoliday}, and
 * {@code formatDistance} in {@code Kenat.ts}.
 */
class DistanceTest {

    @Nested
    @DisplayName("distanceTo")
    class DistanceTo {

        @Test
        void forwardDistanceHasPositiveSign() {
            DiffBreakdown b = new Kenat("2016/1/10").distanceTo(new Kenat("2018/3/15"));
            assertEquals(1, b.sign());
            assertEquals(2, b.years());
            assertEquals(2, b.months());
            assertEquals(5, b.days());
            assertEquals(795, b.totalDays());
        }

        @Test
        void backwardDistanceHasNegativeSign() {
            DiffBreakdown b = new Kenat("2018/3/15").distanceTo(new Kenat("2016/1/10"));
            assertEquals(-1, b.sign());
            assertEquals(795, b.totalDays());
        }

        @Test
        void restrictingUnitsOmitsTheOthers() {
            DiffBreakdown b = new Kenat("2016/1/10")
                    .distanceTo(new Kenat("2018/3/15"), EnumSet.of(DiffUnit.DAYS));
            assertNull(b.years());
            assertNull(b.months());
            assertEquals(795, b.days());
        }

        @Test
        void acceptsLooselyTypedTargets() {
            DiffBreakdown fromString = new Kenat("2016/1/10").distanceTo((Object) "2018/3/15", null);
            DiffBreakdown fromRecord = new Kenat("2016/1/10")
                    .distanceTo((Object) new EthiopianDate(2018, 3, 15), null);
            assertEquals(795, fromString.totalDays());
            assertEquals(795, fromRecord.totalDays());
        }

        @Test
        void sameDayIsZero() {
            DiffBreakdown b = new Kenat("2016/1/10").distanceTo(new Kenat("2016/1/10"));
            assertEquals(0, b.totalDays());
            assertEquals(0, b.years());
            assertEquals(0, b.months());
            assertEquals(0, b.days());
        }
    }

    @Nested
    @DisplayName("distanceFromToday")
    class DistanceFromToday {

        @Test
        void todayIsZeroDaysAway() {
            Kenat today = Kenat.now();
            assertEquals(0, today.distanceFromToday().totalDays());
        }

        @Test
        void aFutureDateIsPositive() {
            Kenat future = Kenat.now().addDays(10);
            DiffBreakdown b = future.distanceFromToday();
            assertEquals(1, b.sign());
            assertEquals(10, b.totalDays());
        }

        @Test
        void aPastDateIsNegative() {
            Kenat past = Kenat.now().addDays(-10);
            DiffBreakdown b = past.distanceFromToday();
            assertEquals(-1, b.sign());
            assertEquals(10, b.totalDays());
        }
    }

    @Nested
    @DisplayName("distanceToHoliday")
    class DistanceToHoliday {

        @Test
        void futureOccurrenceIsOnOrAfterToday() {
            DiffBreakdown b = Kenat.distanceToHoliday(
                    "fasika", Kenat.HolidayDirection.FUTURE, null);
            assertEquals(1, b.sign());
        }

        @Test
        void pastOccurrenceIsOnOrBeforeToday() {
            DiffBreakdown b = Kenat.distanceToHoliday(
                    "fasika", Kenat.HolidayDirection.PAST, null);
            assertTrue(b.sign() == -1 || b.totalDays() == 0);
        }

        @Test
        void autoPicksTheNearerOccurrence() {
            DiffBreakdown auto   = Kenat.distanceToHoliday("fasika");
            DiffBreakdown future = Kenat.distanceToHoliday("fasika", Kenat.HolidayDirection.FUTURE, null);
            DiffBreakdown past   = Kenat.distanceToHoliday("fasika", Kenat.HolidayDirection.PAST, null);
            assertEquals(Math.min(future.totalDays(), past.totalDays()), auto.totalDays());
        }

        @Test
        void worksForFixedHolidaysToo() {
            assertDoesNotThrow(() -> Kenat.distanceToHoliday("enkutatash"));
            assertDoesNotThrow(() -> Kenat.distanceToHoliday("gena"));
        }

        @Test
        void unknownHolidayThrows() {
            assertThrows(UnknownHolidayException.class, () -> Kenat.distanceToHoliday("not-a-holiday"));
        }
    }

    @Nested
    @DisplayName("formatDistance")
    class FormatDistance {

        private final DiffBreakdown breakdown =
                new Kenat("2016/1/10").distanceTo(new Kenat("2018/3/15"));

        @Test
        void englishPluralizes() {
            assertEquals("2 years 2 months 5 days", Kenat.formatDistance(breakdown));
        }

        @Test
        void englishUsesSingularForOne() {
            DiffBreakdown one = new Kenat("2016/1/10").distanceTo(new Kenat("2017/2/11"));
            assertEquals("1 year 1 month 1 day", Kenat.formatDistance(one));
        }

        @Test
        void amharicLabels() {
            assertEquals("2 ዓመት 2 ወር 5 ቀን",
                    Kenat.formatDistance(breakdown, null, "amharic"));
        }

        @Test
        void honoursTheRequestedUnits() {
            DiffBreakdown daysOnly = new Kenat("2016/1/10")
                    .distanceTo(new Kenat("2018/3/15"), EnumSet.of(DiffUnit.DAYS));
            assertEquals("795 days",
                    Kenat.formatDistance(daysOnly, EnumSet.of(DiffUnit.DAYS), "english"));
        }

        @Test
        void fallsBackToZeroOfTheSmallestRequestedUnit() {
            DiffBreakdown empty = new Kenat("2016/1/10")
                    .distanceTo(new Kenat("2018/3/15"), EnumSet.of(DiffUnit.YEARS));
            assertEquals("0 days", Kenat.formatDistance(empty, EnumSet.of(DiffUnit.DAYS), "english"));
        }
    }
}
