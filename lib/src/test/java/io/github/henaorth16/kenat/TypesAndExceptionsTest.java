package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the types-and-errors module: value records, enums, and exception
 * hierarchy.  These are the foundation that every other module depends on.
 */
@DisplayName("Types and Exceptions")
class TypesAndExceptionsTest {

    // -------------------------------------------------------------------------
    // EthiopianDate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("EthiopianDate")
    class EthiopianDateTests {

        @Test
        @DisplayName("stores year, month, and day")
        void storesComponents() {
            var d = new EthiopianDate(2017, 1, 1);
            assertEquals(2017, d.year());
            assertEquals(1,    d.month());
            assertEquals(1,    d.day());
        }

        @Test
        @DisplayName("equals by value")
        void equalsByValue() {
            assertEquals(new EthiopianDate(2017, 9, 14), new EthiopianDate(2017, 9, 14));
        }

        @Test
        @DisplayName("toString produces yyyy/mm/dd")
        void toStringFormat() {
            assertEquals("2017/01/01", new EthiopianDate(2017, 1, 1).toString());
            assertEquals("2017/09/14", new EthiopianDate(2017, 9, 14).toString());
        }

        @Test
        @DisplayName("rejects month 0")
        void rejectsMonthZero() {
            assertThrows(InvalidEthiopianDateException.class, () -> new EthiopianDate(2017, 0, 1));
        }

        @Test
        @DisplayName("rejects month 14")
        void rejectsMonth14() {
            assertThrows(InvalidEthiopianDateException.class, () -> new EthiopianDate(2017, 14, 1));
        }

        @Test
        @DisplayName("rejects day 0")
        void rejectsDayZero() {
            assertThrows(InvalidEthiopianDateException.class, () -> new EthiopianDate(2017, 1, 0));
        }

        @Test
        @DisplayName("rejects day 31 in month 1–12")
        void rejectsDay31() {
            assertThrows(InvalidEthiopianDateException.class, () -> new EthiopianDate(2017, 1, 31));
        }

        @Test
        @DisplayName("allows Pagume day 5 (month 13)")
        void allowsPagumeDay5() {
            assertDoesNotThrow(() -> new EthiopianDate(2017, 13, 5));
        }

        @Test
        @DisplayName("allows Pagume day 6 (month 13, leap)")
        void allowsPagumeDay6() {
            assertDoesNotThrow(() -> new EthiopianDate(2016, 13, 6));
        }

        @Test
        @DisplayName("rejects Pagume day 7")
        void rejectsPagumeDay7() {
            assertThrows(InvalidEthiopianDateException.class, () -> new EthiopianDate(2016, 13, 7));
        }
    }

    // -------------------------------------------------------------------------
    // GregorianDate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GregorianDate")
    class GregorianDateTests {

        @Test
        @DisplayName("stores year, month, and day")
        void storesComponents() {
            var d = new GregorianDate(2025, 5, 22);
            assertEquals(2025, d.year());
            assertEquals(5,    d.month());
            assertEquals(22,   d.day());
        }

        @Test
        @DisplayName("equals by value")
        void equalsByValue() {
            assertEquals(new GregorianDate(2025, 1, 1), new GregorianDate(2025, 1, 1));
        }

        @Test
        @DisplayName("toString produces yyyy-MM-dd")
        void toStringFormat() {
            assertEquals("2025-05-22", new GregorianDate(2025, 5, 22).toString());
        }

        @Test
        @DisplayName("toLocalDate round-trips correctly")
        void toLocalDate() {
            var gd = new GregorianDate(2025, 5, 22);
            assertEquals(LocalDate.of(2025, 5, 22), gd.toLocalDate());
        }

        @Test
        @DisplayName("from(LocalDate) creates equivalent record")
        void fromLocalDate() {
            var ld = LocalDate.of(2024, 11, 1);
            assertEquals(new GregorianDate(2024, 11, 1), GregorianDate.from(ld));
        }

        @Test
        @DisplayName("rejects month 0")
        void rejectsMonthZero() {
            assertThrows(InvalidGregorianDateException.class, () -> new GregorianDate(2025, 0, 1));
        }

        @Test
        @DisplayName("rejects month 13")
        void rejectsMonth13() {
            assertThrows(InvalidGregorianDateException.class, () -> new GregorianDate(2025, 13, 1));
        }

        @Test
        @DisplayName("rejects day 0")
        void rejectsDayZero() {
            assertThrows(InvalidGregorianDateException.class, () -> new GregorianDate(2025, 1, 0));
        }

        @Test
        @DisplayName("rejects day 32")
        void rejectsDay32() {
            assertThrows(InvalidGregorianDateException.class, () -> new GregorianDate(2025, 1, 32));
        }
    }

    // -------------------------------------------------------------------------
    // LocalizedText
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("LocalizedText")
    class LocalizedTextTests {

        @Test
        @DisplayName("get(Language) returns correct language")
        void getByLanguage() {
            var t = new LocalizedText("ፋሲካ", "Easter");
            assertEquals("ፋሲካ", t.get(Language.AMHARIC));
            assertEquals("Easter", t.get(Language.ENGLISH));
        }

        @Test
        @DisplayName("get(String) matches primary languages case-insensitively")
        void getByStringPrimary() {
            var t = new LocalizedText("ፋሲካ", "Easter");
            assertEquals(Optional.of("ፋሲካ"),  t.get("amharic"));
            assertEquals(Optional.of("Easter"), t.get("English"));
        }

        @Test
        @DisplayName("get(String) returns extra locales")
        void getExtras() {
            var t = new LocalizedText("ፋሲካ", "Easter", Map.of("om", "Caamoo"));
            assertEquals(Optional.of("Caamoo"), t.get("om"));
        }

        @Test
        @DisplayName("get(String) returns empty for unknown locale")
        void getMissingLocale() {
            var t = new LocalizedText("ፋሲካ", "Easter");
            assertEquals(Optional.empty(), t.get("fr"));
        }

        @Test
        @DisplayName("extras map is immutable")
        void extrasImmutable() {
            var mutable = new java.util.HashMap<String, String>();
            mutable.put("om", "Caamoo");
            var t = new LocalizedText("ፋሲካ", "Easter", mutable);
            mutable.put("ti", "Fasika"); // mutating source should not affect record
            assertEquals(Optional.empty(), t.get("ti"));
        }
    }

    // -------------------------------------------------------------------------
    // Language enum
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Language")
    class LanguageTests {

        @Test
        @DisplayName("fromString parses amharic case-insensitively")
        void fromStringAmharic() {
            assertEquals(Language.AMHARIC, Language.fromString("amharic"));
            assertEquals(Language.AMHARIC, Language.fromString("AMHARIC"));
        }

        @Test
        @DisplayName("fromString parses english")
        void fromStringEnglish() {
            assertEquals(Language.ENGLISH, Language.fromString("english"));
        }

        @Test
        @DisplayName("fromString throws for unknown locale")
        void fromStringUnknown() {
            assertThrows(IllegalArgumentException.class, () -> Language.fromString("klingon"));
        }

        @Test
        @DisplayName("key() returns lower-case string")
        void key() {
            assertEquals("amharic", Language.AMHARIC.key());
            assertEquals("english", Language.ENGLISH.key());
        }
    }

    // -------------------------------------------------------------------------
    // TimePeriod enum
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("TimePeriod")
    class TimePeriodTests {

        @Test
        @DisplayName("fromString parses day/night case-insensitively")
        void fromString() {
            assertEquals(TimePeriod.DAY,   TimePeriod.fromString("day"));
            assertEquals(TimePeriod.NIGHT, TimePeriod.fromString("NIGHT"));
        }

        @Test
        @DisplayName("fromString throws for unknown period")
        void fromStringUnknown() {
            assertThrows(IllegalArgumentException.class, () -> TimePeriod.fromString("morning"));
        }

        @Test
        @DisplayName("key() returns lower-case string")
        void key() {
            assertEquals("day",   TimePeriod.DAY.key());
            assertEquals("night", TimePeriod.NIGHT.key());
        }
    }

    // -------------------------------------------------------------------------
    // Holiday record
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Holiday")
    class HolidayTests {

        private Holiday makeHoliday() {
            return new Holiday(
                    "fasika",
                    List.of("public", "christian"),
                    true,
                    "Easter",
                    "Ethiopian Orthodox Easter",
                    new EthiopianDate(2017, 7, 27),
                    new GregorianDate(2025, 4, 20)
            );
        }

        @Test
        @DisplayName("stores all fields correctly")
        void storesFields() {
            var h = makeHoliday();
            assertEquals("fasika", h.key());
            assertTrue(h.movable());
            assertEquals("Easter", h.name());
            assertEquals(new EthiopianDate(2017, 7, 27), h.ethiopian());
        }

        @Test
        @DisplayName("hasTag returns true for present tag")
        void hasTag() {
            assertTrue(makeHoliday().hasTag("public"));
            assertTrue(makeHoliday().hasTag("christian"));
            assertFalse(makeHoliday().hasTag("muslim"));
        }

        @Test
        @DisplayName("gregorianDate() wraps non-null gregorian")
        void gregorianDatePresent() {
            assertEquals(Optional.of(new GregorianDate(2025, 4, 20)), makeHoliday().gregorianDate());
        }

        @Test
        @DisplayName("gregorianDate() is empty when gregorian is null")
        void gregorianDateAbsent() {
            var h = new Holiday("key", List.of(), false, "Name", null,
                    new EthiopianDate(2017, 1, 1), null);
            assertEquals(Optional.empty(), h.gregorianDate());
        }

        @Test
        @DisplayName("tags list is immutable")
        void tagsImmutable() {
            var mutableTags = new java.util.ArrayList<>(List.of("public"));
            var h = new Holiday("k", mutableTags, false, "N", null,
                    new EthiopianDate(2017, 1, 1), null);
            mutableTags.add("extra");
            assertFalse(h.hasTag("extra"), "Record should not reflect post-construction mutation");
        }
    }

    // -------------------------------------------------------------------------
    // DateRange
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DateRange")
    class DateRangeTests {

        @Test
        @DisplayName("stores start and end")
        void stores() {
            var r = new DateRange(new EthiopianDate(2017, 1, 1), new EthiopianDate(2017, 1, 30));
            assertEquals(new EthiopianDate(2017, 1, 1),  r.start());
            assertEquals(new EthiopianDate(2017, 1, 30), r.end());
        }

        @Test
        @DisplayName("throws on null start")
        void nullStart() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(null, new EthiopianDate(2017, 1, 1)));
        }

        @Test
        @DisplayName("throws on null end")
        void nullEnd() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DateRange(new EthiopianDate(2017, 1, 1), null));
        }
    }

    // -------------------------------------------------------------------------
    // DiffUnit enum
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DiffUnit")
    class DiffUnitTests {

        @Test
        @DisplayName("key() returns lower-case TS string literal")
        void keys() {
            assertEquals("years",  DiffUnit.YEARS.key());
            assertEquals("months", DiffUnit.MONTHS.key());
            assertEquals("days",   DiffUnit.DAYS.key());
        }

        @Test
        @DisplayName("fromString parses all values case-insensitively")
        void fromString() {
            assertEquals(DiffUnit.YEARS,  DiffUnit.fromString("years"));
            assertEquals(DiffUnit.MONTHS, DiffUnit.fromString("MONTHS"));
            assertEquals(DiffUnit.DAYS,   DiffUnit.fromString("Days"));
        }

        @Test
        @DisplayName("fromString throws for unknown value")
        void fromStringUnknown() {
            assertThrows(IllegalArgumentException.class, () -> DiffUnit.fromString("weeks"));
        }
    }

    // -------------------------------------------------------------------------
    // DiffBreakdown
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DiffBreakdown")
    class DiffBreakdownTests {

        @Test
        @DisplayName("stores all components")
        void storesAll() {
            var b = new DiffBreakdown(1, 395, 1, 1, 5);
            assertEquals(1,   b.sign());
            assertEquals(395, b.totalDays());
            assertEquals(1,   b.years());
            assertEquals(1,   b.months());
            assertEquals(5,   b.days());
        }

        @Test
        @DisplayName("optional accessors return present values")
        void optionalPresent() {
            var b = new DiffBreakdown(1, 395, 1, 1, 5);
            assertEquals(OptionalInt.of(1), b.yearsOpt());
            assertEquals(OptionalInt.of(1), b.monthsOpt());
            assertEquals(OptionalInt.of(5), b.daysOpt());
        }

        @Test
        @DisplayName("optional accessors return empty for null components")
        void optionalAbsent() {
            var b = new DiffBreakdown(1, 10, null, null, null);
            assertEquals(OptionalInt.empty(), b.yearsOpt());
            assertEquals(OptionalInt.empty(), b.monthsOpt());
            assertEquals(OptionalInt.empty(), b.daysOpt());
        }

        @Test
        @DisplayName("rejects invalid sign")
        void invalidSign() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiffBreakdown(0, 10, null, null, null));
        }

        @Test
        @DisplayName("rejects negative totalDays")
        void negativeTotalDays() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiffBreakdown(1, -1, null, null, null));
        }
    }

    // -------------------------------------------------------------------------
    // Exception hierarchy
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Exception hierarchy")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("all exceptions extend KenatException")
        void allExtendKenatException() {
            assertInstanceOf(KenatException.class, new InvalidEthiopianDateException(2017, 14, 1));
            assertInstanceOf(KenatException.class, new InvalidGregorianDateException(2025, 13, 1));
            assertInstanceOf(KenatException.class, new InvalidDateFormatException("bad"));
            assertInstanceOf(KenatException.class, new UnrecognizedInputException("Set"));
            assertInstanceOf(KenatException.class, new GeezConverterException("bad input"));
            assertInstanceOf(KenatException.class, new InvalidInputTypeException("fn", "p", "int", "String"));
            assertInstanceOf(KenatException.class, new InvalidTimeException("bad time"));
            assertInstanceOf(KenatException.class, new InvalidGridConfigException("bad config"));
            assertInstanceOf(KenatException.class, new UnknownHolidayException("UNKNOWN_KEY"));
        }

        @Test
        @DisplayName("all exceptions extend RuntimeException (unchecked)")
        void allUnchecked() {
            assertInstanceOf(RuntimeException.class, new KenatException("test"));
        }

        @Test
        @DisplayName("getType() returns simple class name")
        void getType() {
            assertEquals("InvalidEthiopianDateException",
                    new InvalidEthiopianDateException(2017, 14, 1).getType());
            assertEquals("UnknownHolidayException",
                    new UnknownHolidayException("X").getType());
        }

        @Test
        @DisplayName("InvalidEthiopianDateException message includes date components")
        void invalidEthDateMessage() {
            var ex = new InvalidEthiopianDateException(2017, 14, 1);
            assertTrue(ex.getMessage().contains("2017"));
            assertTrue(ex.getMessage().contains("14"));
            assertEquals(2017, ex.getYear());
            assertEquals(14,   ex.getMonth());
            assertEquals(1,    ex.getDay());
        }

        @Test
        @DisplayName("InvalidEthiopianDateException.of() factory")
        void invalidEthDateFactory() {
            var ex = InvalidEthiopianDateException.of(2017, 0, 1);
            assertEquals(2017, ex.getYear());
        }

        @Test
        @DisplayName("InvalidGregorianDateException message includes date components")
        void invalidGCDateMessage() {
            var ex = new InvalidGregorianDateException(2025, 13, 1);
            assertTrue(ex.getMessage().contains("2025"));
            assertTrue(ex.getMessage().contains("13"));
        }

        @Test
        @DisplayName("InvalidDateFormatException exposes input string")
        void invalidDateFormat() {
            var ex = new InvalidDateFormatException("20170901");
            assertEquals("20170901", ex.getInputString());
            assertTrue(ex.getMessage().contains("yyyy/mm/dd"));
        }

        @Test
        @DisplayName("UnrecognizedInputException exposes input type")
        void unrecognizedInput() {
            var ex = new UnrecognizedInputException("Set");
            assertEquals("Set", ex.getInputType());
        }

        @Test
        @DisplayName("InvalidInputTypeException exposes all fields")
        void invalidInputType() {
            var ex = new InvalidInputTypeException("toGC", "ethMonth", "int", "String");
            assertEquals("toGC",    ex.getFunctionName());
            assertEquals("ethMonth", ex.getParameterName());
            assertEquals("int",     ex.getExpectedType());
            assertEquals("String",  ex.getReceivedType());
            assertTrue(ex.getMessage().contains("toGC"));
            assertTrue(ex.getMessage().contains("ethMonth"));
        }

        @Test
        @DisplayName("UnknownHolidayException exposes holiday key")
        void unknownHoliday() {
            var ex = new UnknownHolidayException("FAKE_HOLIDAY");
            assertEquals("FAKE_HOLIDAY", ex.getHolidayKey());
            assertTrue(ex.getMessage().contains("FAKE_HOLIDAY"));
        }
    }
}
