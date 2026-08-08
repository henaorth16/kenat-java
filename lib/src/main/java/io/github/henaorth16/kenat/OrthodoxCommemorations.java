package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.internal.Json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orthodox Tewahedo monthly commemorations ("Nigs" days), grouped by day of month.
 * Mirrors {@code nigs.json} and its consumption in {@code MonthGrid.ts}.
 *
 * <p>Every day of the Ethiopian month (1–30) carries a set of saints and feasts
 * commemorated on that day in <em>every</em> month.  A commemoration becomes a
 * <em>nigs</em> (major monthly feast) only in the specific months listed in its
 * {@link Commemoration#negs()} array — e.g. Ledeta Mariam is commemorated on the
 * 1st of every month but is a nigs only in Ginbot (month 9).
 *
 * <p>The data is read once from the classpath resource
 * {@code /kenat/nigs.json} on first use and cached for the lifetime of the JVM.
 */
public final class OrthodoxCommemorations {

    private static final String RESOURCE = "/kenat/nigs.json";

    private OrthodoxCommemorations() {}

    /**
     * A single Orthodox commemoration recurring on a given day of the month.
     *
     * @param id          stable snake_case identifier, e.g. {@code "ledeta_mariam"}
     * @param name        localized display name
     * @param description localized description
     * @param major       whether the commemoration is a major one
     * @param negs        Ethiopian months (1–13) in which this day is a nigs
     */
    public record Commemoration(
            String id,
            LocalizedText name,
            LocalizedText description,
            boolean major,
            List<Integer> negs
    ) {
        /** Canonical constructor — makes {@code negs} immutable. */
        public Commemoration {
            negs = negs == null ? List.of() : List.copyOf(negs);
        }

        /**
         * Returns {@code true} if this commemoration is a nigs in the given month.
         *
         * @param ethiopianMonth Ethiopian month (1–13)
         */
        public boolean isNigsIn(int ethiopianMonth) {
            return negs.contains(ethiopianMonth);
        }
    }

    /** Lazily-initialised holder — loads the resource on first access. */
    private static final class Holder {
        static final Map<Integer, List<Commemoration>> BY_DAY = load();
    }

    /**
     * Returns every commemoration, keyed by day of month (1–30).
     *
     * @return unmodifiable day-to-commemorations map
     */
    public static Map<Integer, List<Commemoration>> byDay() {
        return Holder.BY_DAY;
    }

    /**
     * Returns the commemorations recorded for the given day of month.
     *
     * @param dayOfMonth Ethiopian day of month (1–30)
     * @return unmodifiable list, empty when the day has no entries
     */
    public static List<Commemoration> forDay(int dayOfMonth) {
        return Holder.BY_DAY.getOrDefault(dayOfMonth, List.of());
    }

    // -----------------------------------------------------------------------
    // Loading
    // -----------------------------------------------------------------------

    private static Map<Integer, List<Commemoration>> load() {
        String text;
        try (InputStream in = OrthodoxCommemorations.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing bundled resource: " + RESOURCE);
            }
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read bundled resource: " + RESOURCE, e);
        }

        Map<String, Object> root = Json.parseObject(text);
        Map<Integer, List<Commemoration>> byDay = new LinkedHashMap<>();

        for (Map.Entry<String, Object> dayEntry : root.entrySet()) {
            int day;
            try {
                day = Integer.parseInt(dayEntry.getKey());
            } catch (NumberFormatException e) {
                continue; // ignore non-numeric keys rather than failing the whole load
            }
            List<Commemoration> entries = new ArrayList<>();
            for (Object raw : asList(dayEntry.getValue())) {
                Commemoration c = toCommemoration(raw);
                if (c != null) entries.add(c);
            }
            byDay.put(day, Collections.unmodifiableList(entries));
        }
        return Collections.unmodifiableMap(byDay);
    }

    private static Commemoration toCommemoration(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return null;

        String id = str(map.get("id"));
        if (id == null) return null;

        LocalizedText name = localized(map.get("name"));
        LocalizedText description = localized(map.get("description"));
        boolean major = Boolean.TRUE.equals(map.get("major"));

        List<Integer> negs = new ArrayList<>();
        for (Object n : asList(map.get("negs"))) {
            if (n instanceof Number num) negs.add(num.intValue());
        }
        return new Commemoration(id, name, description, major, negs);
    }

    private static LocalizedText localized(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return new LocalizedText("", "");
        String amharic = str(map.get("amharic"));
        String english = str(map.get("english"));
        return new LocalizedText(amharic == null ? "" : amharic, english == null ? "" : english);
    }

    private static List<?> asList(Object raw) {
        return raw instanceof List<?> list ? list : List.of();
    }

    private static String str(Object raw) {
        return raw instanceof String s ? s : null;
    }
}
