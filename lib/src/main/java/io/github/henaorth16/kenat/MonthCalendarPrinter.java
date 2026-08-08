package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.InvalidInputTypeException;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Renders a month calendar as plain text.
 * Mirrors {@code render/printMonthCalendarGrid.ts}.
 *
 * <p>Each cell shows the Ethiopian day and its Gregorian counterpart as
 * {@code "ET/GR"}, laid out Monday-first:
 * <pre>
 *    መስከረም 2016
 * Mo  Tu  We  Th  Fr  Sa  Su
 *                       1/12  2/13
 *  3/14  4/15 ...
 * </pre>
 */
public final class MonthCalendarPrinter {

    private static final List<String> DAYS_OF_WEEK =
            List.of("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su");

    private MonthCalendarPrinter() {}

    /**
     * Renders the grid to the given stream.
     *
     * @param ethiopianYear  Ethiopian year
     * @param ethiopianMonth Ethiopian month (1–13)
     * @param calendarData   the month's days, as produced by
     *                       {@link Kenat#getMonthCalendar(int, int, boolean)}
     * @param useGeez        render the year and day numbers in Ge'ez numerals
     * @param out            destination stream
     * @throws InvalidInputTypeException if {@code ethiopianMonth} is outside 1–13
     */
    public static void print(int ethiopianYear, int ethiopianMonth,
                             List<Kenat.CalendarDay> calendarData,
                             boolean useGeez, PrintStream out) {
        if (ethiopianMonth < 1 || ethiopianMonth > 13) {
            throw InvalidInputTypeException.forValue(
                    "printMonthCalendarGrid", "ethiopianMonth", "number between 1 and 13", ethiopianMonth);
        }
        if (calendarData == null || calendarData.isEmpty()) {
            out.println("Calendar data is empty or invalid. Cannot print grid.");
            return;
        }

        out.println();
        out.println("   " + KenatConstants.MONTH_NAMES_AMHARIC.get(ethiopianMonth - 1) + " "
                + (useGeez ? GeezConverter.toGeez(ethiopianYear) : String.valueOf(ethiopianYear)));
        out.println(String.join("  ", DAYS_OF_WEEK));

        GregorianDate first = calendarData.get(0).gregorian();
        LocalDate firstDate = LocalDate.of(first.year(), first.month(), first.day());
        int weekday = firstDate.getDayOfWeek().getValue() - 1; // Monday is 0

        StringBuilder row = new StringBuilder();
        int inRow = 0;
        for (int i = 0; i < weekday; i++) {
            appendCell(row, "    ", inRow++);
        }

        for (Kenat.CalendarDay day : calendarData) {
            String ethDay = useGeez
                    ? padStart(GeezConverter.toGeez(day.ethiopian().day()), 2, '፩')
                    : padStart(String.valueOf(day.ethiopian().day()), 2, ' ');
            String gregDay = padStart(String.valueOf(day.gregorian().day()), 2, ' ');
            appendCell(row, ethDay + "/" + gregDay, inRow++);

            if (inRow == 7) {
                out.println(row);
                row.setLength(0);
                inRow = 0;
            }
        }

        if (inRow > 0) {
            out.println(padEnd(row.toString(), 27));
        }
    }

    /**
     * Renders the grid to {@link System#out}.
     *
     * @param ethiopianYear  Ethiopian year
     * @param ethiopianMonth Ethiopian month (1–13)
     * @param calendarData   the month's days
     * @param useGeez        render the year and day numbers in Ge'ez numerals
     */
    public static void print(int ethiopianYear, int ethiopianMonth,
                             List<Kenat.CalendarDay> calendarData, boolean useGeez) {
        print(ethiopianYear, ethiopianMonth, calendarData, useGeez, System.out);
    }

    private static void appendCell(StringBuilder row, String cell, int index) {
        if (index > 0) row.append(' ');
        row.append(cell);
    }

    private static String padStart(String s, int length, char pad) {
        if (s.length() >= length) return s;
        return String.valueOf(pad).repeat(length - s.length()) + s;
    }

    private static String padEnd(String s, int length) {
        if (s.length() >= length) return s;
        return s + " ".repeat(length - s.length());
    }
}
