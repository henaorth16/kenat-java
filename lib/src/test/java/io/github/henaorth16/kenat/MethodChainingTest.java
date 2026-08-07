package io.github.henaorth16.kenat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/methodChaining.test.ts}.
 *
 * <p>{@code Kenat} is immutable, so every arithmetic method returns a new
 * instance and calls can be chained freely.
 */
class MethodChainingTest {

    @Test
    void chainsAddOperations() {
        Kenat future = new Kenat("2017/1/1")
                .add(7, "days")
                .add(1, "months")
                .add(1, "years");
        assertEquals(new EthiopianDate(2018, 2, 8), future.getEthiopian());
    }

    @Test
    void chainsSubtractOperations() {
        Kenat past = new Kenat("2017/1/1")
                .subtract(7, "days")
                .subtract(1, "months")
                .subtract(1, "years");
        assertEquals(new EthiopianDate(2015, 11, 29), past.getEthiopian());
    }

    @Test
    void chainsMixedAddAndSubtract() {
        Kenat result = new Kenat("2017/1/1")
                .add(7, "days")
                .subtract(1, "months")
                .add(1, "years");
        assertEquals(new EthiopianDate(2017, 13, 5), result.getEthiopian());
    }

    @Test
    void preservesTimeAcrossArithmetic() {
        Kenat date = new Kenat(new EthiopianDate(2017, 1, 1),
                new EthiopianTime(3, 30, TimePeriod.DAY));
        Kenat future = date.add(7, "days").add(1, "months");

        assertEquals(new EthiopianDate(2017, 2, 8), future.getEthiopian());
        assertEquals(3, future.getTime().hour());
        assertEquals(30, future.getTime().minute());
        assertEquals(TimePeriod.DAY, future.getTime().period());
    }

    @Test
    void chainsStartOfAndEndOf() {
        Kenat result = new Kenat("2017/6/15")
                .startOf("month")
                .add(7, "days")
                .endOf("day");

        assertEquals(new EthiopianDate(2017, 6, 8), result.getEthiopian());
        assertEquals(12, result.getTime().hour());
        assertEquals(TimePeriod.NIGHT, result.getTime().period());
    }

    @Test
    void chainsSetTime() {
        Kenat result = new Kenat("2017/1/1")
                .add(7, "days")
                .setTime(6, 30, TimePeriod.NIGHT)
                .add(1, "months");

        assertEquals(new EthiopianDate(2017, 2, 8), result.getEthiopian());
        assertEquals(6, result.getTime().hour());
        assertEquals(30, result.getTime().minute());
        assertEquals(TimePeriod.NIGHT, result.getTime().period());
    }

    @Test
    void originalInstanceIsNeverMutated() {
        Kenat original = new Kenat("2017/1/1");
        Kenat modified = original.add(7, "days").add(1, "months");

        assertEquals(new EthiopianDate(2017, 1, 1), original.getEthiopian());
        assertEquals(new EthiopianDate(2017, 2, 8), modified.getEthiopian());
    }

    @Test
    void handlesLeapYearInChaining() {
        Kenat result = new Kenat("2015/13/6") // leap-year Pagume
                .add(1, "days")
                .add(1, "months");
        assertEquals(new EthiopianDate(2016, 2, 1), result.getEthiopian());
    }

    @Test
    void handlesMonthBoundariesInChaining() {
        Kenat result = new Kenat("2017/1/30") // last day of Meskerem
                .add(1, "days")
                .add(1, "months");
        assertEquals(new EthiopianDate(2017, 3, 1), result.getEthiopian());
    }

    @Test
    void supportsLongChains() {
        Kenat result = new Kenat("2017/1/1")
                .startOf("month")
                .add(14, "days")
                .setTime(12, 0, TimePeriod.DAY)
                .add(1, "months")
                .endOf("day")
                .add(1, "years");

        assertEquals(new EthiopianDate(2018, 2, 15), result.getEthiopian());
        assertEquals(12, result.getTime().hour());
        assertEquals(TimePeriod.NIGHT, result.getTime().period());
    }

    @Test
    void rejectsInvalidUnitInAdd() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Kenat("2017/1/1").add(1, "weeks"));
        assertTrue(e.getMessage().contains("Invalid unit: weeks"));
    }

    @Test
    void rejectsInvalidUnitInStartOf() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Kenat("2017/1/1").startOf("week"));
        assertTrue(e.getMessage().contains("Invalid unit: week"));
    }

    @Test
    void rejectsInvalidUnitInEndOf() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Kenat("2017/1/1").endOf("week"));
        assertTrue(e.getMessage().contains("Invalid unit: week"));
    }
}
