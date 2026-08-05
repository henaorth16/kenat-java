package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.internal.Json;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the bundled {@code kenat/nigs.json} resource, its loader
 * {@link OrthodoxCommemorations}, and the {@link Json} reader underneath.
 */
class OrthodoxCommemorationsTest {

    @Nested
    @DisplayName("Loaded data")
    class Data {

        @Test
        void coversEveryDayOfAThirtyDayMonth() {
            Map<Integer, List<OrthodoxCommemorations.Commemoration>> byDay = OrthodoxCommemorations.byDay();
            for (int day = 1; day <= 30; day++) {
                assertTrue(byDay.containsKey(day), "no entry for day " + day);
                assertFalse(byDay.get(day).isEmpty(), "empty entry for day " + day);
            }
        }

        @Test
        void entriesAreFullyPopulated() {
            OrthodoxCommemorations.byDay().values().stream()
                    .flatMap(List::stream)
                    .forEach(c -> {
                        assertNotNull(c.id());
                        assertFalse(c.id().isBlank());
                        assertNotNull(c.name());
                        assertFalse(c.name().amharic().isBlank());
                        assertFalse(c.name().english().isBlank());
                        assertNotNull(c.description());
                        assertNotNull(c.negs());
                        c.negs().forEach(m -> assertTrue(m >= 1 && m <= 13, "bad negs month: " + m));
                    });
        }

        @Test
        void ledetaMariamIsANigsInGinbotOnly() {
            OrthodoxCommemorations.Commemoration ledeta = OrthodoxCommemorations.forDay(1).stream()
                    .filter(c -> c.id().equals("ledeta_mariam"))
                    .findFirst().orElseThrow();

            assertEquals("ልደታ ማርያም", ledeta.name().amharic());
            assertTrue(ledeta.isNigsIn(9));
            assertFalse(ledeta.isNigsIn(1));
        }

        @Test
        void unknownDaysReturnAnEmptyList() {
            assertTrue(OrthodoxCommemorations.forDay(31).isEmpty());
            assertTrue(OrthodoxCommemorations.forDay(0).isEmpty());
        }

        @Test
        void loadedCollectionsAreUnmodifiable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> OrthodoxCommemorations.byDay().clear());
            assertThrows(UnsupportedOperationException.class,
                    () -> OrthodoxCommemorations.forDay(1).clear());
        }
    }

    @Nested
    @DisplayName("Json reader")
    class JsonReader {

        @Test
        void parsesScalars() {
            assertEquals("hi", Json.parse("\"hi\""));
            assertEquals(42L, Json.parse("42"));
            assertEquals(-1.5, Json.parse("-1.5"));
            assertEquals(Boolean.TRUE, Json.parse("true"));
            assertEquals(Boolean.FALSE, Json.parse("false"));
            assertNull(Json.parse("null"));
        }

        @Test
        void parsesNestedStructures() {
            Map<String, Object> root = Json.parseObject(
                    "{\"a\": [1, 2, {\"b\": \"c\"}], \"d\": {\"e\": null}}");
            assertEquals(List.of(1L, 2L, Map.of("b", "c")), root.get("a"));
            assertTrue(((Map<?, ?>) root.get("d")).containsKey("e"));
        }

        @Test
        void parsesEmptyContainers() {
            assertEquals(Map.of(), Json.parse("{}"));
            assertEquals(List.of(), Json.parse("[]"));
        }

        @Test
        void handlesEscapeSequences() {
            assertEquals("a\"b\\c/d\be\ff\ng\rh\ti",
                    Json.parse("\"a\\\"b\\\\c\\/d\\be\\ff\\ng\\rh\\ti\""));
            assertEquals("ሀ", Json.parse("\"\\u1200\""));
        }

        @Test
        void ignoresWhitespace() {
            assertEquals(Map.of("a", 1L), Json.parse("  {\n\t\"a\" :\r 1 }  "));
        }

        @Test
        void rejectsMalformedInput() {
            assertThrows(Json.JsonException.class, () -> Json.parse("{"));
            assertThrows(Json.JsonException.class, () -> Json.parse("{\"a\": }"));
            assertThrows(Json.JsonException.class, () -> Json.parse("[1, 2"));
            assertThrows(Json.JsonException.class, () -> Json.parse("\"unterminated"));
            assertThrows(Json.JsonException.class, () -> Json.parse("tru"));
            assertThrows(Json.JsonException.class, () -> Json.parse("1 2"));
            assertThrows(Json.JsonException.class, () -> Json.parse(null));
            assertThrows(Json.JsonException.class, () -> Json.parseObject("[]"));
        }
    }
}
