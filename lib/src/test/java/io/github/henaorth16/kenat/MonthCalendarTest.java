package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidInputTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/print.test.ts}, plus coverage for
 * {@link MonthCalendarPrinter} (the Java counterpart of
 * {@code render/printMonthCalendarGrid.ts}).
 */
class MonthCalendarTest {

    @Nested
    @DisplayName("Kenat.getMonthCalendar")
    class GetMonthCalendar {

        @Test
        void standardMonthHas30Days() {
            assertEquals(30, new Kenat("2015/1/1").getMonthCalendar(2015, 1, false).size());
        }

        @Test
        void pagumeHas6DaysInALeapYear() {
            assertEquals(6, new Kenat("2011/13/1").getMonthCalendar(2011, 13, false).size());
        }

        @Test
        void pagumeHas5DaysInACommonYear() {
            assertEquals(5, new Kenat("2012/13/1").getMonthCalendar(2012, 13, false).size());
        }

        @Test
        void eachDayCarriesBothCalendarsAndDisplayStrings() {
            Kenat.CalendarDay day = new Kenat("2015/2/1").getMonthCalendar(2015, 2, false).get(0);
            assertNotNull(day.ethiopian());
            assertNotNull(day.gregorian());
            assertNotNull(day.ethDisplay());
            assertNotNull(day.gregDisplay());
            assertFalse(day.ethDisplay().isBlank());
            assertEquals(String.format("%d-%02d-%02d",
                            day.gregorian().year(), day.gregorian().month(), day.gregorian().day()),
                    day.gregDisplay());
        }

        @Test
        void geezNumeralsAreUsedWhenRequested() {
            Kenat.CalendarDay day = new Kenat("2015/1/1").getMonthCalendar(2015, 1, true).get(0);
            assertTrue(day.ethDisplay().matches(".*[፩-፼].*"),
                    "expected Ge'ez numerals in: " + day.ethDisplay());
        }

        @Test
        void instanceOverloadUsesThisInstancesMonth() {
            assertEquals(30, new Kenat("2015/1/5").getMonthCalendar().size());
        }
    }

    @Nested
    @DisplayName("MonthCalendarPrinter")
    class Printer {

        private String render(int year, int month, boolean useGeez) {
            List<Kenat.CalendarDay> days = Kenat.getMonthCalendarStatic(year, month, useGeez);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            MonthCalendarPrinter.print(year, month, days, useGeez,
                    new PrintStream(buffer, true, StandardCharsets.UTF_8));
            return buffer.toString(StandardCharsets.UTF_8);
        }

        @Test
        void rendersHeaderAndEveryDay() {
            String output = render(2016, 1, false);
            assertTrue(output.contains("መስከረም 2016"));
            assertTrue(output.contains("Mo  Tu  We  Th  Fr  Sa  Su"));
            for (int day = 1; day <= 30; day++) {
                assertTrue(output.contains(String.format("%2d/", day)),
                        "missing day " + day + " in:\n" + output);
            }
        }

        @Test
        void rendersGeezNumerals() {
            String output = render(2016, 1, true);
            assertTrue(output.contains("፳፻፲፮"));
        }

        @Test
        void rejectsOutOfRangeMonth() {
            List<Kenat.CalendarDay> days = Kenat.getMonthCalendarStatic(2016, 1, false);
            assertThrows(InvalidInputTypeException.class,
                    () -> MonthCalendarPrinter.print(2016, 0, days, false, System.out));
            assertThrows(InvalidInputTypeException.class,
                    () -> MonthCalendarPrinter.print(2016, 14, days, false, System.out));
        }

        @Test
        void handlesEmptyInputGracefully() {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            MonthCalendarPrinter.print(2016, 1, List.of(), false,
                    new PrintStream(buffer, true, StandardCharsets.UTF_8));
            assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("empty or invalid"));
        }

        @Test
        void printThisMonthDoesNotThrow() {
            PrintStream original = System.out;
            try {
                System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
                assertDoesNotThrow(() -> new Kenat("2016/1/1").printThisMonth());
                assertDoesNotThrow(() -> new Kenat("2016/1/1").printThisMonth(true));
            } finally {
                System.setOut(original);
            }
        }
    }
}
