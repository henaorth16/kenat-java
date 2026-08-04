package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.UnknownHolidayException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/bahireHasab.test.ts}.
 *
 * <p>Every expected value here is a regression fixture taken from the
 * TypeScript library and must not be adjusted to make Java pass.
 */
class BahireHasabTest {

    @Nested
    @DisplayName("getBahireHasab for 2016 E.C.")
    class Year2016 {

        private final BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(2016);

        @Test
        void ameteAlemAndMeteneRabiet() {
            assertEquals(7516, bh.ameteAlem());
            assertEquals(1879, bh.meteneRabiet());
        }

        @Test
        void evangelist() {
            assertEquals("ዮሐንስ", bh.evangelistName());
            assertEquals(0, bh.evangelistRemainder());
        }

        @Test
        void newYearDayName() {
            assertEquals("ማክሰኞ", bh.newYearWeekday());
        }

        @Test
        void medebWenberAbektieMetqi() {
            assertEquals(11, bh.medeb());
            assertEquals(10, bh.wenber());
            assertEquals(20, bh.abektie());
            assertEquals(10, bh.metqi());
        }

        @Test
        void ninevehDate() {
            assertEquals(new EthiopianDate(2016, 6, 18), bh.ninevehDate());
        }
    }

    /**
     * Regression test for kenat issue #36 — "Fix Bahire Hasab Logic for Metqi 30
     * Edge Case (e.g., 2006 E.C.)".
     */
    @Nested
    @DisplayName("getBahireHasab for 2006 E.C. (Metqi 30/0 edge case)")
    class Year2006 {

        private final BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(2006);

        @Test
        void metqiOfZeroIsTreatedAs30() {
            assertDoesNotThrow(() -> BahireHasab.getBahireHasab(2006));
            assertEquals(30, bh.metqi());
        }

        @Test
        void bealeMetqiLandsOnMeskerem30() {
            assertEquals(new EthiopianDate(2006, 1, 30), bh.bealeMetqiDate());
        }

        @Test
        void ninevehMonthRollsOverWhenTewsakSumExceeds30() {
            assertEquals(3, bh.mebajaHamer());
            assertEquals(new EthiopianDate(2006, 6, 3), bh.ninevehDate());
        }

        @Test
        void fasika2006() {
            Holiday fasika = bh.movableFeasts().get("fasika");
            assertNotNull(fasika);
            assertEquals(new EthiopianDate(2006, 8, 12), fasika.ethiopian());
            assertEquals(new GregorianDate(2014, 4, 20), fasika.gregorian());
        }
    }

    @Nested
    @DisplayName("Internationalization")
    class I18n {

        @Test
        void englishNames() {
            BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(2016, "english");
            assertEquals("John", bh.evangelistName());
            assertEquals("Tuesday", bh.newYearWeekday());
        }
    }

    @Nested
    @DisplayName("Movable feasts")
    class MovableFeasts {

        private final BahireHasab.BahireHasabResult bh = BahireHasab.getBahireHasab(2016, "english");

        @Test
        void returnsCompleteFeastMap() {
            assertNotNull(bh.movableFeasts());
            assertTrue(bh.movableFeasts().size() > 5);
        }

        @Test
        void fasika() {
            Holiday fasika = bh.movableFeasts().get("fasika");
            assertNotNull(fasika);
            assertEquals(new EthiopianDate(2016, 8, 27), fasika.ethiopian());
            assertEquals("Ethiopian Easter", fasika.name());
            assertTrue(fasika.hasTag(HolidayTags.PUBLIC));
        }

        @Test
        void abiyTsome() {
            Holiday abiyTsome = bh.movableFeasts().get("abiyTsome");
            assertNotNull(abiyTsome);
            assertEquals(new EthiopianDate(2016, 7, 2), abiyTsome.ethiopian());
            assertEquals("Great Lent", abiyTsome.name());
        }

        @Test
        void feastAccessorWrapsLookup() {
            assertTrue(bh.feast("fasika").isPresent());
            assertTrue(bh.feast("nope").isEmpty());
        }

        @Test
        void movableFeastMapIsUnmodifiable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> bh.movableFeasts().remove("fasika"));
        }
    }

    @Nested
    @DisplayName("Error handling")
    class Errors {

        @Test
        void unknownHolidayKeyThrows() {
            assertThrows(UnknownHolidayException.class,
                    () -> BahireHasab.getMovableHoliday("UNKNOWN_HOLIDAY", 2016));
        }
    }
}
