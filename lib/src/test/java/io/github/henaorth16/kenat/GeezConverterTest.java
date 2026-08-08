package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.GeezConverterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ported from {@code ../kenat/tests/geezConverter.test.ts} and
 * {@code ../kenat/tests/ethiopianNumberConverter.test.ts}.
 */
class GeezConverterTest {

    @Nested
    @DisplayName("toGeez")
    class ToGeez {

        @Test
        void singleDigits() {
            assertEquals("፩", GeezConverter.toGeez(1));
            assertEquals("፪", GeezConverter.toGeez(2));
            assertEquals("፭", GeezConverter.toGeez(5));
            assertEquals("፱", GeezConverter.toGeez(9));
        }

        @Test
        void tens() {
            assertEquals("፲", GeezConverter.toGeez(10));
            assertEquals("፳", GeezConverter.toGeez(20));
            assertEquals("፴", GeezConverter.toGeez(30));
            assertEquals("፺፱", GeezConverter.toGeez(99));
        }

        @Test
        void hundreds() {
            assertEquals("፻", GeezConverter.toGeez(100));
            assertEquals("፻፩", GeezConverter.toGeez(101));
            assertEquals("፻፲", GeezConverter.toGeez(110));
            assertEquals("፻፳፫", GeezConverter.toGeez(123));
            assertEquals("፱፻፺፱", GeezConverter.toGeez(999));
        }

        @Test
        void thousandsAndTenThousands() {
            assertEquals("፲፻", GeezConverter.toGeez(1000));
            assertEquals("፼", GeezConverter.toGeez(10_000));
            assertEquals("፼፳፫፻፵፭", GeezConverter.toGeez(12_345));
            assertEquals("፱፼፺፱፻፺፱", GeezConverter.toGeez(99_999));
        }

        @Test
        void zeroRendersAsAsciiZero() {
            assertEquals("0", GeezConverter.toGeez(0));
        }

        @Test
        void negativeInputThrows() {
            assertThrows(GeezConverterException.class, () -> GeezConverter.toGeez(-1));
            assertThrows(GeezConverterException.class, () -> GeezConverter.toGeez(-1L));
        }
    }

    @Nested
    @DisplayName("toArabic")
    class ToArabic {

        @Test
        void singleDigits() {
            assertEquals(1, GeezConverter.toArabic("፩"));
            assertEquals(5, GeezConverter.toArabic("፭"));
            assertEquals(9, GeezConverter.toArabic("፱"));
        }

        @Test
        void tens() {
            assertEquals(10, GeezConverter.toArabic("፲"));
            assertEquals(20, GeezConverter.toArabic("፳"));
            assertEquals(30, GeezConverter.toArabic("፴"));
            assertEquals(99, GeezConverter.toArabic("፺፱"));
        }

        @Test
        void hundreds() {
            assertEquals(100, GeezConverter.toArabic("፻"));
            assertEquals(101, GeezConverter.toArabic("፻፩"));
            assertEquals(110, GeezConverter.toArabic("፻፲"));
            assertEquals(123, GeezConverter.toArabic("፻፳፫"));
            assertEquals(999, GeezConverter.toArabic("፱፻፺፱"));
        }

        @Test
        void thousandsAndTenThousands() {
            assertEquals(1000, GeezConverter.toArabic("፲፻"));
            assertEquals(10_000, GeezConverter.toArabic("፼"));
            assertEquals(100_000, GeezConverter.toArabic("፲፼"));
            assertEquals(12_345, GeezConverter.toArabic("፼፳፫፻፵፭"));
            assertEquals(99_999, GeezConverter.toArabic("፱፼፺፱፻፺፱"));
        }

        @Test
        void complexNumbers() {
            assertEquals(1099, GeezConverter.toArabic("፲፻፺፱"));
            assertEquals(400, GeezConverter.toArabic("፬፻"));
        }

        @Test
        void unknownNumeralThrows() {
            GeezConverterException e = assertThrows(GeezConverterException.class,
                    () -> GeezConverter.toArabic("A"));
            assertTrue(e.getMessage().contains("A"));
            assertThrows(GeezConverterException.class, () -> GeezConverter.toArabic("፩X"));
            assertThrows(GeezConverterException.class, () -> GeezConverter.toArabic("xyz"));
            assertThrows(GeezConverterException.class, () -> GeezConverter.toArabic("፻፻፻x"));
        }

        @Test
        void blankInputIsZero() {
            assertEquals(0, GeezConverter.toArabic(""));
            assertEquals(0, GeezConverter.toArabic("   "));
        }

        @Test
        void nullInputThrows() {
            // geezConverter.ts throws for a non-string input but returns 0 for an
            // empty string; the two cases stay distinct here.
            assertThrows(GeezConverterException.class, () -> GeezConverter.toArabic(null));
        }
    }

    @Test
    @DisplayName("round-trips toGeez → toArabic")
    void roundTrip() {
        for (int n : new int[]{1, 10, 99, 100, 123, 999, 1000, 10_000, 12_345, 999_999}) {
            assertEquals(n, GeezConverter.toArabic(GeezConverter.toGeez(n)), "round trip failed for " + n);
        }
    }
}
