package io.github.henaorth16.kenat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link KenatConstants}, {@link HolidayTags}, and
 * {@link FastingKeys}.
 *
 * Coverage strategy: verify sizes, spot-check representative values, confirm
 * immutability guards, and cross-check the key-to-tewsak mapping.
 */
class ConstantsTest {

    // -----------------------------------------------------------------------
    // HolidayTags
    // -----------------------------------------------------------------------

    @Test
    void holidayTagValues() {
        assertEquals("public",    HolidayTags.PUBLIC);
        assertEquals("religious", HolidayTags.RELIGIOUS);
        assertEquals("christian", HolidayTags.CHRISTIAN);
        assertEquals("muslim",    HolidayTags.MUSLIM);
        assertEquals("state",     HolidayTags.STATE);
        assertEquals("cultural",  HolidayTags.CULTURAL);
        assertEquals("other",     HolidayTags.OTHER);
    }

    // -----------------------------------------------------------------------
    // FastingKeys
    // -----------------------------------------------------------------------

    @Test
    void fastingKeyValues() {
        assertEquals("ABIY_TSOME",     FastingKeys.ABIY_TSOME);
        assertEquals("TSOME_HAWARYAT", FastingKeys.TSOME_HAWARYAT);
        assertEquals("TSOME_NEBIYAT",  FastingKeys.TSOME_NEBIYAT);
        assertEquals("NINEVEH",        FastingKeys.NINEVEH);
        assertEquals("FILSETA",        FastingKeys.FILSETA);
        assertEquals("RAMADAN",        FastingKeys.RAMADAN);
        assertEquals("TSOME_DIHENET",  FastingKeys.TSOME_DIHENET);
    }

    // -----------------------------------------------------------------------
    // Month names
    // -----------------------------------------------------------------------

    @Test
    void monthNamesEnglishSize() {
        assertEquals(13, KenatConstants.MONTH_NAMES_ENGLISH.size());
    }

    @Test
    void monthNamesEnglishSpotCheck() {
        assertEquals("Meskerem", KenatConstants.MONTH_NAMES_ENGLISH.get(0));
        assertEquals("Pagume",   KenatConstants.MONTH_NAMES_ENGLISH.get(12));
        assertEquals("Sene",     KenatConstants.MONTH_NAMES_ENGLISH.get(9));
    }

    @Test
    void monthNamesAmharicSize() {
        assertEquals(13, KenatConstants.MONTH_NAMES_AMHARIC.size());
    }

    @Test
    void monthNamesAmharicSpotCheck() {
        assertEquals("መስከረም", KenatConstants.MONTH_NAMES_AMHARIC.get(0));
        assertEquals("ጳጉሜ",   KenatConstants.MONTH_NAMES_AMHARIC.get(12));
    }

    @Test
    void monthNamesGregorianSize() {
        assertEquals(12, KenatConstants.MONTH_NAMES_GREGORIAN.size());
    }

    @Test
    void monthNamesGregorianSpotCheck() {
        assertEquals("January",  KenatConstants.MONTH_NAMES_GREGORIAN.get(0));
        assertEquals("December", KenatConstants.MONTH_NAMES_GREGORIAN.get(11));
    }

    @Test
    void monthNamesImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> KenatConstants.MONTH_NAMES_ENGLISH.add("Extra"));
    }

    // -----------------------------------------------------------------------
    // Days of week
    // -----------------------------------------------------------------------

    @Test
    void daysOfWeekEnglishSize() {
        assertEquals(7, KenatConstants.DAYS_OF_WEEK_ENGLISH.size());
    }

    @Test
    void daysOfWeekEnglishSpotCheck() {
        assertEquals("Sunday",   KenatConstants.DAYS_OF_WEEK_ENGLISH.get(0));
        assertEquals("Saturday", KenatConstants.DAYS_OF_WEEK_ENGLISH.get(6));
    }

    @Test
    void daysOfWeekAmharicSize() {
        assertEquals(7, KenatConstants.DAYS_OF_WEEK_AMHARIC.size());
    }

    @Test
    void daysOfWeekAmharicSpotCheck() {
        assertEquals("እሑድ", KenatConstants.DAYS_OF_WEEK_AMHARIC.get(0));
        assertEquals("ቅዳሜ", KenatConstants.DAYS_OF_WEEK_AMHARIC.get(6));
    }

    // -----------------------------------------------------------------------
    // Period labels
    // -----------------------------------------------------------------------

    @Test
    void periodLabels() {
        assertEquals("ጠዋት", KenatConstants.PERIOD_LABEL_DAY);
        assertEquals("ማታ", KenatConstants.PERIOD_LABEL_NIGHT);
    }

    // -----------------------------------------------------------------------
    // Evangelist names
    // -----------------------------------------------------------------------

    @Test
    void evangelistNamesEnglish() {
        assertEquals("Matthew", KenatConstants.EVANGELIST_NAMES_ENGLISH.get(1));
        assertEquals("Mark",    KenatConstants.EVANGELIST_NAMES_ENGLISH.get(2));
        assertEquals("Luke",    KenatConstants.EVANGELIST_NAMES_ENGLISH.get(3));
        assertEquals("John",    KenatConstants.EVANGELIST_NAMES_ENGLISH.get(0));
    }

    @Test
    void evangelistNamesAmharic() {
        assertEquals("ማቴዎስ", KenatConstants.EVANGELIST_NAMES_AMHARIC.get(1));
        assertEquals("ማርቆስ", KenatConstants.EVANGELIST_NAMES_AMHARIC.get(2));
        assertEquals("ሉቃስ",  KenatConstants.EVANGELIST_NAMES_AMHARIC.get(3));
        assertEquals("ዮሐንስ", KenatConstants.EVANGELIST_NAMES_AMHARIC.get(0));
    }

    // -----------------------------------------------------------------------
    // Tewsak maps
    // -----------------------------------------------------------------------

    @Test
    void tewsakMapValues() {
        assertEquals(7, KenatConstants.TEWSAK_MAP.get("Sunday"));
        assertEquals(6, KenatConstants.TEWSAK_MAP.get("Monday"));
        assertEquals(5, KenatConstants.TEWSAK_MAP.get("Tuesday"));
        assertEquals(4, KenatConstants.TEWSAK_MAP.get("Wednesday"));
        assertEquals(3, KenatConstants.TEWSAK_MAP.get("Thursday"));
        assertEquals(2, KenatConstants.TEWSAK_MAP.get("Friday"));
        assertEquals(8, KenatConstants.TEWSAK_MAP.get("Saturday"));
    }

    @Test
    void movableHolidayTewsakValues() {
        assertEquals(0,   KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get("NINEVEH"));
        assertEquals(14,  KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get("ABIY_TSOME"));
        assertEquals(69,  KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get("TINSAYE"));
        assertEquals(118, KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get("PARACLETE"));
        assertEquals(121, KenatConstants.MOVABLE_HOLIDAY_TEWSAK.get("TSOME_DIHENET"));
    }

    @Test
    void movableHolidayTewsakSize() {
        assertEquals(11, KenatConstants.MOVABLE_HOLIDAY_TEWSAK.size());
    }

    @Test
    void keyToTewsakMap() {
        assertEquals("TINSAYE",       KenatConstants.KEY_TO_TEWSAK_MAP.get("fasika"));
        assertEquals("NINEVEH",       KenatConstants.KEY_TO_TEWSAK_MAP.get("nineveh"));
        assertEquals("ABIY_TSOME",    KenatConstants.KEY_TO_TEWSAK_MAP.get("abiyTsome"));
        assertEquals("TSOME_HAWARYAT",KenatConstants.KEY_TO_TEWSAK_MAP.get("tsomeHawaryat"));
    }

    // -----------------------------------------------------------------------
    // Movable holidays tag map
    // -----------------------------------------------------------------------

    @Test
    void movableHolidaysSize() {
        assertEquals(14, KenatConstants.MOVABLE_HOLIDAYS.size());
    }

    @Test
    void movableHolidaysTagsSpotCheck() {
        List<String> fasikaTagsList = KenatConstants.MOVABLE_HOLIDAYS.get("fasika");
        assertNotNull(fasikaTagsList);
        assertTrue(fasikaTagsList.contains(HolidayTags.PUBLIC));
        assertTrue(fasikaTagsList.contains(HolidayTags.CHRISTIAN));

        List<String> eidFitrTags = KenatConstants.MOVABLE_HOLIDAYS.get("eidFitr");
        assertNotNull(eidFitrTags);
        assertTrue(eidFitrTags.contains(HolidayTags.MUSLIM));
    }

    // -----------------------------------------------------------------------
    // Holiday info
    // -----------------------------------------------------------------------

    @Test
    void holidayInfoSize() {
        assertEquals(24, KenatConstants.HOLIDAY_INFO.size());
    }

    @Test
    void holidayInfoSpotCheck() {
        KenatConstants.HolidayInfo enkutatash = KenatConstants.HOLIDAY_INFO.get("enkutatash");
        assertNotNull(enkutatash);
        assertEquals("Ethiopian New Year (Enkutatash)", enkutatash.name().english());
        assertEquals("እንቁጣጣሽ", enkutatash.name().amharic());
    }

    @Test
    void holidayInfoFasikaDescription() {
        KenatConstants.HolidayInfo fasika = KenatConstants.HOLIDAY_INFO.get("fasika");
        assertNotNull(fasika);
        assertTrue(fasika.description().english().contains("resurrection"));
    }

    @Test
    void holidayInfoJummah() {
        KenatConstants.HolidayInfo jummah = KenatConstants.HOLIDAY_INFO.get("jummah");
        assertNotNull(jummah);
        assertEquals("Jummah", jummah.name().english());
        assertEquals("ጁምዓ", jummah.name().amharic());
    }

    @Test
    void holidayNamesSetContainsAllKeys() {
        Map<String, String> names = KenatConstants.HOLIDAY_NAMES;
        // Each key maps to itself — mirrors TS HolidayNames record
        assertEquals("enkutatash", names.get("enkutatash"));
        assertEquals("meskel",     names.get("meskel"));
        assertEquals("fasika",     names.get("fasika"));
        assertEquals("eidFitr",    names.get("eidFitr"));
        assertEquals("jummah",     names.get("jummah"));
    }

    @Test
    void holidayInfoImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> KenatConstants.HOLIDAY_INFO.put("extra", null));
    }

    // -----------------------------------------------------------------------
    // Fasting info
    // -----------------------------------------------------------------------

    @Test
    void fastingInfoSize() {
        assertEquals(7, KenatConstants.FASTING_INFO.size());
    }

    @Test
    void fastingInfoSpotCheck() {
        KenatConstants.FastingInfo lent = KenatConstants.FASTING_INFO.get(FastingKeys.ABIY_TSOME);
        assertNotNull(lent);
        assertEquals("Great Lent (Abiy Tsome)", lent.name().english());
        assertEquals("ዐቢይ ጾም (ሁዳዴ)", lent.name().amharic());
        assertTrue(lent.tags().contains(HolidayTags.CHRISTIAN));
    }

    @Test
    void fastingInfoRamadanIsMuslim() {
        KenatConstants.FastingInfo ramadan = KenatConstants.FASTING_INFO.get(FastingKeys.RAMADAN);
        assertNotNull(ramadan);
        assertTrue(ramadan.tags().contains(HolidayTags.MUSLIM));
        assertFalse(ramadan.tags().contains(HolidayTags.CHRISTIAN));
    }

    @Test
    void fastingInfoTagsImmutable() {
        KenatConstants.FastingInfo info = KenatConstants.FASTING_INFO.get(FastingKeys.NINEVEH);
        assertNotNull(info);
        assertThrows(UnsupportedOperationException.class,
                () -> info.tags().add("extra"));
    }

    @Test
    void fastingInfoMapImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> KenatConstants.FASTING_INFO.put("EXTRA", null));
    }
}
