package io.github.henaorth16.kenat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/dayArtimetic.test.ts}, exercised through the
 * {@link Kenat} façade exactly as the TypeScript suite does.
 */
class DayArithmeticTest {

    @Nested
    @DisplayName("addDays")
    class AddDays {

        @Test
        void withinSameMonth() {
            assertEquals(new EthiopianDate(2016, 1, 15),
                    new Kenat("2016/01/10").addDays(5).getEthiopian());
        }

        @Test
        void crossingMonthBoundary() {
            assertEquals(new EthiopianDate(2016, 2, 3),
                    new Kenat("2016/01/28").addDays(5).getEthiopian());
        }

        @Test
        void crossingYearBoundary() {
            assertEquals(new EthiopianDate(2017, 1, 2),
                    new Kenat("2016/13/4").addDays(3).getEthiopian());
        }

        @Test
        void exactlyAtMonthEnd() {
            assertEquals(new EthiopianDate(2016, 2, 30),
                    new Kenat("2016/02/25").addDays(5).getEthiopian());
        }

        @Test
        void zeroDaysReturnsSameDate() {
            assertEquals(new EthiopianDate(2016, 5, 15),
                    new Kenat("2016/05/15").addDays(0).getEthiopian());
        }

        @Test
        void acrossPagumeIntoNextYear() {
            assertEquals(new EthiopianDate(2017, 1, 2),
                    new Kenat("2016/13/1").addDays(6).getEthiopian());
        }

        @Test
        void negativeDaysMoveBackwards() {
            assertEquals(new EthiopianDate(2016, 1, 5),
                    new Kenat("2016/01/10").addDays(-5).getEthiopian());
        }
    }

    @Nested
    @DisplayName("addMonths")
    class AddMonths {

        @Test
        void withinSameYear() {
            assertEquals(new EthiopianDate(2016, 8, 10),
                    new Kenat("2016/03/10").addMonths(5).getEthiopian());
        }

        @Test
        void withYearRollover() {
            assertEquals(new EthiopianDate(2017, 1, 10),
                    new Kenat("2016/11/10").addMonths(3).getEthiopian());
        }

        @Test
        void intoPagumeClampsDay() {
            // 2015 is an Ethiopian leap year, so Pagume has 6 days
            assertEquals(new EthiopianDate(2015, 13, 6),
                    new Kenat("2015/12/30").addMonths(1).getEthiopian());
        }

        @Test
        void intoPagumeInLeapYear() {
            assertEquals(new EthiopianDate(2011, 13, 6),
                    new Kenat("2011/12/30").addMonths(1).getEthiopian());
        }

        @Test
        void zeroMonthsReturnsSameDate() {
            assertEquals(new EthiopianDate(2016, 6, 20),
                    new Kenat("2016/06/20").addMonths(0).getEthiopian());
        }

        @Test
        void negativeMonthsAcrossYearBoundary() {
            assertEquals(new EthiopianDate(2015, 12, 10),
                    new Kenat("2016/03/10").addMonths(-4).getEthiopian());
        }

        @Test
        void subtractIntoPagumeWithClamping() {
            assertEquals(new EthiopianDate(2015, 13, 6),
                    new Kenat("2016/01/06").addMonths(-1).getEthiopian());
        }
    }

    @Nested
    @DisplayName("addYears")
    class AddYears {

        @Test
        void withinLeapSafeMonth() {
            assertEquals(new EthiopianDate(2013, 5, 15),
                    new Kenat("2010/05/15").addYears(3).getEthiopian());
        }

        @Test
        void fromLeapPagume6ToNonLeapYearClampsDay() {
            assertEquals(new EthiopianDate(2012, 13, 5),
                    new Kenat("2011/13/6").addYears(1).getEthiopian());
        }

        @Test
        void toAnotherLeapYearKeepsPagume6() {
            assertEquals(new EthiopianDate(2015, 13, 6),
                    new Kenat("2011/13/6").addYears(4).getEthiopian());
        }

        @Test
        void zeroYearsReturnsSameDate() {
            assertEquals(new EthiopianDate(2016, 3, 10),
                    new Kenat("2016/03/10").addYears(0).getEthiopian());
        }

        @Test
        void subtractAcrossLeapTransition() {
            assertEquals(new EthiopianDate(2014, 13, 5),
                    new Kenat("2015/13/6").addYears(-1).getEthiopian());
        }
    }

    @Nested
    @DisplayName("diffInDays")
    class DiffInDays {

        @Test
        void sameDateIsZero() {
            assertEquals(0, new Kenat("2016/05/15").diffInDays(new Kenat("2016/05/15")));
        }

        @Test
        void laterMinusEarlierIsPositive() {
            assertEquals(5, new Kenat("2016/06/10").diffInDays(new Kenat("2016/06/05")));
        }

        @Test
        void earlierMinusLaterIsNegative() {
            assertEquals(-5, new Kenat("2016/06/01").diffInDays(new Kenat("2016/06/06")));
        }

        @Test
        void crossingYearBoundary() {
            assertEquals(4, new Kenat("2017/01/03").diffInDays(new Kenat("2016/13/04")));
        }

        @Test
        void crossingMultipleYears() {
            assertEquals(730, new Kenat("2018/01/01").diffInDays(new Kenat("2016/01/01")));
        }
    }

    @Nested
    @DisplayName("diffInMonths")
    class DiffInMonths {

        @Test
        void sameDateIsZero() {
            assertEquals(0, new Kenat("2016/05/15").diffInMonths(new Kenat("2016/05/15")));
        }

        @Test
        void laterMinusEarlierWithinYear() {
            assertEquals(1, new Kenat("2016/06/10").diffInMonths(new Kenat("2016/05/05")));
        }

        @Test
        void earlierMinusLaterWithinYear() {
            // -1 raw, minus another month because day 1 < day 6
            assertEquals(-2, new Kenat("2016/05/01").diffInMonths(new Kenat("2016/06/06")));
        }

        @Test
        void crossingYearBoundary() {
            assertEquals(0, new Kenat("2017/01/03").diffInMonths(new Kenat("2016/13/04")));
        }

        @Test
        void crossingYearBoundaryWithoutDayAdjustment() {
            assertEquals(1, new Kenat("2017/01/05").diffInMonths(new Kenat("2016/13/04")));
        }

        @Test
        void crossingMultipleYears() {
            assertEquals(26, new Kenat("2018/01/01").diffInMonths(new Kenat("2016/01/01")));
        }
    }

    @Nested
    @DisplayName("diffInYears")
    class DiffInYears {

        @Test
        void sameDateIsZero() {
            assertEquals(0, new Kenat("2016/05/15").diffInYears(new Kenat("2016/05/15")));
        }

        @Test
        void withinSameYearIsZero() {
            assertEquals(0, new Kenat("2016/06/10").diffInYears(new Kenat("2016/05/05")));
            assertEquals(0, new Kenat("2016/05/01").diffInYears(new Kenat("2016/06/06")));
        }

        @Test
        void crossingYearBoundaryWithDayAdjustment() {
            assertEquals(0, new Kenat("2017/01/03").diffInYears(new Kenat("2016/13/04")));
        }

        @Test
        void crossingMultipleYears() {
            assertEquals(2, new Kenat("2018/01/01").diffInYears(new Kenat("2016/01/01")));
        }

        @Test
        void earlierMinusLaterIsNegative() {
            assertEquals(-2, new Kenat("2016/01/01").diffInYears(new Kenat("2018/01/01")));
        }
    }

    @Nested
    @DisplayName("diffBreakdown")
    class Breakdown {

        @Test
        void decomposesIntoYearsMonthsDays() {
            DiffBreakdown b = DayArithmetic.diffBreakdown(
                    new EthiopianDate(2018, 3, 15), new EthiopianDate(2016, 1, 10), null);
            assertEquals(1, b.sign());
            assertEquals(2, b.years());
            assertEquals(2, b.months());
            assertEquals(5, b.days());
            assertEquals(795, b.totalDays());
        }

        @Test
        void signIsNegativeWhenFirstDateIsEarlier() {
            DiffBreakdown b = DayArithmetic.diffBreakdown(
                    new EthiopianDate(2016, 1, 10), new EthiopianDate(2018, 3, 15), null);
            assertEquals(-1, b.sign());
            assertEquals(795, b.totalDays());
        }

        @Test
        void omittedUnitsAreNull() {
            DiffBreakdown b = DayArithmetic.diffBreakdown(
                    new EthiopianDate(2018, 3, 15), new EthiopianDate(2016, 1, 10),
                    EnumSet.of(DiffUnit.DAYS));
            assertNull(b.years());
            assertNull(b.months());
            assertEquals(795, b.days());
            assertTrue(b.yearsOpt().isEmpty());
            assertTrue(b.daysOpt().isPresent());
        }

        @Test
        void identicalDatesGiveZeroesWithPositiveSign() {
            DiffBreakdown b = DayArithmetic.diffBreakdown(
                    new EthiopianDate(2016, 1, 1), new EthiopianDate(2016, 1, 1), null);
            assertEquals(1, b.sign());
            assertEquals(0, b.totalDays());
            assertEquals(0, b.years());
            assertEquals(0, b.months());
            assertEquals(0, b.days());
        }
    }
}
