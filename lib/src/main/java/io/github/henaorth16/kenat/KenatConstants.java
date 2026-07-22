package io.github.henaorth16.kenat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All static lookup tables for the Kenat library.
 * Mirrors the exports in {@code constants.ts}.
 *
 * <p>Every field is an unmodifiable collection so callers cannot mutate
 * shared state.  The class is final with a private constructor — use the
 * public static fields directly.
 */
public final class KenatConstants {

    private KenatConstants() {}

    /** Ethiopian month names in English (indices 0–12, month 1 = index 0). */
    public static final List<String> MONTH_NAMES_ENGLISH = List.of(
            "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
            "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    );

    /** Ethiopian month names in Amharic (indices 0–12). */
    public static final List<String> MONTH_NAMES_AMHARIC = List.of(
            "መስከረም", "ጥቅምት", "ህዳር", "ታህሳስ", "ጥር", "የካቲት",
            "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሀምሌ", "ነሐሴ", "ጳጉሜ"
    );

    /** Gregorian month names in English (indices 0–11, January = index 0). */
    public static final List<String> MONTH_NAMES_GREGORIAN = List.of(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    // -----------------------------------------------------------------------
    // Days of week
    // -----------------------------------------------------------------------

    /** Day-of-week names in English (Sunday = index 0). */
    public static final List<String> DAYS_OF_WEEK_ENGLISH = List.of(
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    );

    /** Day-of-week names in Amharic (Sunday = index 0). */
    public static final List<String> DAYS_OF_WEEK_AMHARIC = List.of(
            "እሑድ", "ሰኞ", "ማክሰኞ", "ረቡዕ", "ሐሙስ", "ዓርብ", "ቅዳሜ"
    );

    // -----------------------------------------------------------------------
    // Period labels
    // -----------------------------------------------------------------------

    /** Amharic label for the daytime period (ጠዋት). */
    public static final String PERIOD_LABEL_DAY   = "ጠዋት";

    /** Amharic label for the nighttime period (ማታ). */
    public static final String PERIOD_LABEL_NIGHT = "ማታ";

    // -----------------------------------------------------------------------
    // Evangelist names  (used by Bahire Hasab)
    // -----------------------------------------------------------------------

    /**
     * Maps remainder value (0–3) to evangelist name in English.
     * Key 0 = John, 1 = Matthew, 2 = Mark, 3 = Luke.
     */
    public static final Map<Integer, String> EVANGELIST_NAMES_ENGLISH = Map.of(
            0, "John",
            1, "Matthew",
            2, "Mark",
            3, "Luke"
    );

    /**
     * Maps remainder value (0–3) to evangelist name in Amharic.
     * Key 0 = ዮሐንስ, 1 = ማቴዎስ, 2 = ማርቆስ, 3 = ሉቃስ.
     */
    public static final Map<Integer, String> EVANGELIST_NAMES_AMHARIC = Map.of(
            0, "ዮሐንስ",
            1, "ማቴዎስ",
            2, "ማርቆስ",
            3, "ሉቃስ"
    );

    // -----------------------------------------------------------------------
    // Tewsak tables  (used by Bahire Hasab to derive Nineveh date)
    // -----------------------------------------------------------------------

    /**
     * Maps English day-of-week name to tewsak offset used to compute the
     * Nineveh start date from the Bahire Hasab base Sunday.
     * Mirrors {@code tewsakMap} in {@code constants.ts}.
     */
    public static final Map<String, Integer> TEWSAK_MAP = Map.of(
            "Sunday",    7,
            "Monday",    6,
            "Tuesday",   5,
            "Wednesday", 4,
            "Thursday",  3,
            "Friday",    2,
            "Saturday",  8
    );

    /**
     * Maps movable-holiday key (e.g. {@code "TINSAYE"}) to its day-offset
     * from Nineveh.  Mirrors {@code movableHolidayTewsak} in
     * {@code constants.ts}.
     */
    public static final Map<String, Integer> MOVABLE_HOLIDAY_TEWSAK = Map.ofEntries(
            Map.entry("NINEVEH",        0),
            Map.entry("ABIY_TSOME",    14),
            Map.entry("DEBRE_ZEIT",    41),
            Map.entry("HOSANNA",       62),
            Map.entry("SIKLET",        67),
            Map.entry("TINSAYE",       69),
            Map.entry("RIKBE_KAHNAT",  93),
            Map.entry("ERGET",        108),
            Map.entry("PARACLETE",    118),
            Map.entry("TSOME_HAWARYAT", 119),
            Map.entry("TSOME_DIHENET", 121)
    );

    /**
     * Maps camelCase holiday key (as used in {@code holidayInfo}) to the
     * uppercase internal tewsak key used in {@link #MOVABLE_HOLIDAY_TEWSAK}.
     * Mirrors {@code keyToTewsakMap} in {@code constants.ts}.
     */
    public static final Map<String, String> KEY_TO_TEWSAK_MAP = Map.ofEntries(
            Map.entry("nineveh",        "NINEVEH"),
            Map.entry("abiyTsome",      "ABIY_TSOME"),
            Map.entry("debreZeit",      "DEBRE_ZEIT"),
            Map.entry("hosanna",        "HOSANNA"),
            Map.entry("siklet",         "SIKLET"),
            Map.entry("fasika",         "TINSAYE"),
            Map.entry("rikbeKahnat",    "RIKBE_KAHNAT"),
            Map.entry("erget",          "ERGET"),
            Map.entry("paraclete",      "PARACLETE"),
            Map.entry("tsomeHawaryat",  "TSOME_HAWARYAT"),
            Map.entry("tsomeDihnet",    "TSOME_DIHENET")
    );

    // -----------------------------------------------------------------------
    // Movable-holiday tag map
    // -----------------------------------------------------------------------

    /**
     * Maps camelCase holiday key to its list of classification tags.
     * Mirrors {@code movableHolidays} in {@code constants.ts}.
     */
    public static final Map<String, List<String>> MOVABLE_HOLIDAYS = Map.ofEntries(
            Map.entry("nineveh",       List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("abiyTsome",     List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("debreZeit",     List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("hosanna",       List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("siklet",        List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN, HolidayTags.PUBLIC)),
            Map.entry("fasika",        List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("rikbeKahnat",   List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("erget",         List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("paraclete",     List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("tsomeHawaryat", List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("tsomeDihnet",   List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)),
            Map.entry("eidFitr",       List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.MUSLIM)),
            Map.entry("eidAdha",       List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.MUSLIM)),
            Map.entry("moulid",        List.of(HolidayTags.PUBLIC, HolidayTags.RELIGIOUS, HolidayTags.MUSLIM))
    );

    // -----------------------------------------------------------------------
    // Holiday info  (name + description in both languages)
    // -----------------------------------------------------------------------

    /**
     * Info record bundling a holiday's localised name and description.
     *
     * @param name        localised display name
     * @param description localised description
     */
    public record HolidayInfo(LocalizedText name, LocalizedText description) {}

    /**
     * Maps camelCase holiday key to its {@link HolidayInfo}.
     * Mirrors {@code holidayInfo} in {@code constants.ts}.
     */
    public static final Map<String, HolidayInfo> HOLIDAY_INFO;

    static {
        Map<String, HolidayInfo> m = new LinkedHashMap<>();

        m.put("enkutatash", new HolidayInfo(
                new LocalizedText("እንቁጣጣሽ", "Ethiopian New Year (Enkutatash)"),
                new LocalizedText(
                        "የኢትዮጵያ አዲስ ዓመት መጀመሪያ፤ የዝናብ ወቅት ማብቃቱን እና ዳግም መታደስን ያመለክታል።",
                        "Marks the start of the Ethiopian year; symbolizes renewal and the end of the rainy season."
                )));

        m.put("meskel", new HolidayInfo(
                new LocalizedText("መስቀል", "Finding of the True Cross (Meskel)"),
                new LocalizedText(
                        "በ4ኛው መቶ ክፍለ ዘመን በንግሥት እሌኒ አማካኝነት የጌታችን መስቀል መገኘቱን ያከብራል።",
                        "Commemorates the discovery of the True Cross by Empress Helena in the 4th century."
                )));

        m.put("beherbehereseb", new HolidayInfo(
                new LocalizedText("የብሔር ብሔረሰቦች ቀን", "Nations, Nationalities, and Peoples' Day"),
                new LocalizedText(
                        "የኢትዮጵያ ብሔር ብሔረሰቦችን ልዩነት የሚያከብር፣ እኩል መብታቸውን የሚያረጋግጥ እና በባህልና ቋንቋ አንድነትን የሚያጠናክር በዓል ነው።",
                        "Acknowledges and celebrates the diversity of Ethiopia's ethnic groups, affirming their equal rights and fostering unity."
                )));

        m.put("gena", new HolidayInfo(
                new LocalizedText("ገና", "Ethiopian Christmas (Genna)"),
                new LocalizedText(
                        "የኢየሱስ ክርስቶስን ልደት የሚያከብር የኢትዮጵያ ኦርቶዶክስ ተዋሕዶ ቤተ ክርስቲያን በዓል።",
                        "Ethiopian Orthodox Christmas celebrating the birth of Jesus Christ."
                )));

        m.put("timket", new HolidayInfo(
                new LocalizedText("ጥምቀት", "Ethiopian Epiphany (Timket)"),
                new LocalizedText(
                        "የኢየሱስ ክርስቶስን በዮርዳኖስ ወንዝ መጠመቁን ያከብራል።",
                        "Commemorates the baptism of Jesus in the Jordan River."
                )));

        m.put("martyrsDay", new HolidayInfo(
                new LocalizedText("የሰማዕታት ቀን", "Martyrs' Day"),
                new LocalizedText(
                        "ለኢትዮጵያ ነፃነትና ክብር ሕይወታቸውን የሠዉ ሰማዕታትን ያስባል።",
                        "Honors those who sacrificed their lives for Ethiopia's freedom and independence."
                )));

        m.put("adwa", new HolidayInfo(
                new LocalizedText("የአድዋ ድል በዓል", "Victory of Adwa"),
                new LocalizedText(
                        "በ1896 ዓ.ም. ኢትዮጵያ በጣሊያን ቅኝ ገዥዎች ላይ የተቀዳጀችውን ድል ያከብራል።",
                        "Celebrates Ethiopia's victory over Italian colonizers in 1896."
                )));

        m.put("labour", new HolidayInfo(
                new LocalizedText("የሰራተኞች ቀን", "International Labour Day"),
                new LocalizedText(
                        "ዓለም አቀፍ የሠራተኞችና የሥራ መብቶች ቀን ነው።",
                        "A global celebration of workers and labor rights."
                )));

        m.put("patriots", new HolidayInfo(
                new LocalizedText("የአርበኞች ቀን", "Patriots' Victory Day"),
                new LocalizedText(
                        "የጣሊያን ወረራን የተቋቋሙ ኢትዮጵያውያን አርበኞችን ድል ያስባል።",
                        "Honors Ethiopian resistance fighters who defeated Italian occupation."
                )));

        m.put("nineveh", new HolidayInfo(
                new LocalizedText("ጾመ ነነዌ", "Fast of Nineveh"),
                new LocalizedText(
                        "የነነዌ ሰዎች ንስሐ መግባታቸውን የሚያስታውስ የሦስት ቀን ጾም ነው።",
                        "A three-day fast commemorating the repentance of the people of Nineveh."
                )));

        m.put("abiyTsome", new HolidayInfo(
                new LocalizedText("ዐቢይ ጾም", "Great Lent"),
                new LocalizedText(
                        "ከፋሲካ በፊት የሚጾም የ55 ቀናት የጾም ወቅት ነው።",
                        "The Great Lent, a 55-day fasting period before Easter."
                )));

        m.put("debreZeit", new HolidayInfo(
                new LocalizedText("ደብረ ዘይት", "Mid-Lent Sunday"),
                new LocalizedText(
                        "ኢየሱስ በደብረ ዘይት ተራራ ያስተማረውን ትምህርት የሚያስታውስ የዐቢይ ጾም አጋማሽ እሑድ።",
                        "Mid-Lent Sunday, commemorating Jesus's sermon on the Mount of Olives."
                )));

        m.put("hosanna", new HolidayInfo(
                new LocalizedText("ሆሳዕና", "Palm Sunday"),
                new LocalizedText(
                        "ኢየሱስ በክብር ወደ ኢየሩሳሌም መግባቱን የሚያስታውስ በዓል።",
                        "Palm Sunday, commemorating Jesus's triumphal entry into Jerusalem."
                )));

        m.put("siklet", new HolidayInfo(
                new LocalizedText("ስቅለት", "Good Friday"),
                new LocalizedText(
                        "የኢየሱስ ክርስቶስን ስቅለት የሚያስታውስ ነው።",
                        "Marks the crucifixion of Jesus Christ."
                )));

        m.put("fasika", new HolidayInfo(
                new LocalizedText("ፋሲካ", "Ethiopian Easter"),
                new LocalizedText(
                        "የኢየሱስ ክርስቶስን ከሙታን መነሣት ያከብራል። በኢትዮጵያ ውስጥ ካሉ ክርስቲያናዊ በዓላት አንዱና ዋነኛው ነው።",
                        "Celebrates the resurrection of Jesus Christ. One of the most important Christian holidays in Ethiopia."
                )));

        m.put("rikbeKahnat", new HolidayInfo(
                new LocalizedText("ርክበ ካህናት", "Meeting of the Priests"),
                new LocalizedText(
                        "ከፋሲካ 24 ቀናት በኋላ የሚከበር የካህናት መሰባሰብ በዓል ነው።",
                        "The Meeting of the Priests, 24 days after Easter."
                )));

        m.put("erget", new HolidayInfo(
                new LocalizedText("ዕርገት", "Ascension"),
                new LocalizedText(
                        "ከፋሲካ 40 ቀናት በኋላ ኢየሱስ ወደ ሰማይ ማረጉን ያከብራል።",
                        "The Ascension of Jesus into heaven, 40 days after Easter."
                )));

        m.put("paraclete", new HolidayInfo(
                new LocalizedText("ጰራቅሊጦስ", "Pentecost"),
                new LocalizedText(
                        "መንፈስ ቅዱስ በሐዋርያት ላይ መውረዱን የሚያከብር በዓል፣ ከፋሲካ 50 ቀናት በኋላ።",
                        "Pentecost, celebrating the descent of the Holy Spirit upon the Apostles, 50 days after Easter."
                )));

        m.put("tsomeHawaryat", new HolidayInfo(
                new LocalizedText("ጾመ ሐዋርያት", "Apostles' Fast"),
                new LocalizedText(
                        "ከጰራቅሊጦስ ማግስት የሚጀምር የሐዋርያት ጾም ነው።",
                        "The Fast of the Apostles, which begins the day after Pentecost."
                )));

        m.put("tsomeDihnet", new HolidayInfo(
                new LocalizedText("ጾመ ድኅነት", "Fast of Salvation"),
                new LocalizedText(
                        "በየሳምንቱ ረቡዕ እና ዓርብ የሚጾም የድኅነት ጾም ነው።",
                        "The Fast of Salvation, observed on Wednesdays and Fridays."
                )));

        m.put("eidFitr", new HolidayInfo(
                new LocalizedText("ዒድ አል ፈጥር", "Eid al-Fitr"),
                new LocalizedText(
                        "የረመዳን ጾም ወር መገባደድን የሚያመለክት በዓል።",
                        "Marks the end of Ramadan, the month of fasting for Muslims."
                )));

        m.put("eidAdha", new HolidayInfo(
                new LocalizedText("ዒድ አል አድሐ", "Eid al-Adha"),
                new LocalizedText(
                        "አብርሃም ለእግዚአብሔር በመታዘዝ ልጁን ለመሠዋት ፈቃደኝነቱን የሚያስታውስ በዓል።",
                        "Commemorates Abraham's willingness to sacrifice his son as an act of obedience to God."
                )));

        m.put("moulid", new HolidayInfo(
                new LocalizedText("መውሊድ", "Birth of the Prophet"),
                new LocalizedText(
                        "የነቢዩ ሙሐመድን የልደት በዓል ያከብራል።",
                        "Celebrates the birthday of the Prophet Mohammed."
                )));

        m.put("jummah", new HolidayInfo(
                new LocalizedText("ጁምዓ", "Jummah"),
                new LocalizedText(
                        "ሳምንታዊ የሙስሊሞች የጋራ ስግደት።",
                        "The weekly Muslim congregational prayer."
                )));

        HOLIDAY_INFO = Collections.unmodifiableMap(m);
    }

    /**
     * Maps each holiday key to itself — a stable set of named constants that
     * avoids hardcoding raw strings in caller code.
     *
     * <p>Mirrors {@code HolidayNames} in {@code constants.ts}, which is a
     * {@code Record<string,string>} where every value equals its key
     * (e.g. {@code HolidayNames.fasika === "fasika"}).
     */
    public static final Map<String, String> HOLIDAY_NAMES;

    static {
        Map<String, String> m = new LinkedHashMap<>(HOLIDAY_INFO.size());
        for (String key : HOLIDAY_INFO.keySet()) {
            m.put(key, key);
        }
        HOLIDAY_NAMES = Collections.unmodifiableMap(m);
    }

    // -----------------------------------------------------------------------
    // Fasting info
    // -----------------------------------------------------------------------

    /**
     * Info record bundling a fasting period's localised name, description,
     * and classification tags.
     *
     * @param name        localised display name
     * @param description localised description
     * @param tags        classification tags (see {@link HolidayTags})
     */
    public record FastingInfo(LocalizedText name, LocalizedText description, List<String> tags) {
        public FastingInfo {
            tags = List.copyOf(tags);
        }
    }

    /**
     * Maps uppercase fasting key (see {@link FastingKeys}) to its
     * {@link FastingInfo}.  Mirrors {@code fastingInfo} in
     * {@code constants.ts}.
     */
    public static final Map<String, FastingInfo> FASTING_INFO;

    static {
        Map<String, FastingInfo> m = new LinkedHashMap<>();

        m.put(FastingKeys.ABIY_TSOME, new FastingInfo(
                new LocalizedText("ዐቢይ ጾም (ሁዳዴ)", "Great Lent (Abiy Tsome)"),
                new LocalizedText(
                        "ከፋሲካ በፊት የሚጾም 55 ቀናት የጾም ወቅት ነው።",
                        "A 55-day fasting season observed before Easter in the Ethiopian Orthodox Church."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        m.put(FastingKeys.NINEVEH, new FastingInfo(
                new LocalizedText("ጾመ ነነዌ", "Fast of Nineveh"),
                new LocalizedText(
                        "የነነዌ ሰዎች ለሰሩት ሐጢያት የእግዚአብሔርን ምህረትና ይቅርታ ለመጠየቅ የፆሙትን ለማስታወስ ነው።",
                        "A three-day fast commemorating the repentance of the people of Nineveh."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        m.put(FastingKeys.TSOME_HAWARYAT, new FastingInfo(
                new LocalizedText("ጾመ ሐዋርያት", "Apostles' Fast (Tsome Hawaryat)"),
                new LocalizedText(
                        "ክርስቶስ ከሙታን ተለይቶ ከተነሳ በሃላ ቅዱሳን ሀዋርያት ሰማያዊ ተልእኮ ስለተሰማቸው ከ50ቀን በሃላ በበአለ ጰራቅሊጦስ ማግስት መፆም ጀመሩ።",
                        "Begins after Pentecost and lasts until Hamle 4 in the Ethiopian calendar."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        m.put(FastingKeys.TSOME_NEBIYAT, new FastingInfo(
                new LocalizedText("የገና-ፆም (ጾመ ነቢያት)", "Fast of the Prophets (Tsome Nebiyat)"),
                new LocalizedText(
                        "ከህዳር 15 እስከ ታሕሳስ 28 ድረስ የሚጠብቅ የቋሚ ጾም ነው።",
                        "A fixed fast observed from Hidar 15 to Tahsas 28."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        m.put(FastingKeys.FILSETA, new FastingInfo(
                new LocalizedText("ፍልሰታ", "Filseta"),
                new LocalizedText(
                        "ፆመ ፍልሰታ /ከነሐሴ 1 እስከ ነሐሴ 14 ቀን/ የእመቤታችን ዕረፍት፣ ትንሣኤና ዕርገቷ የሚታሰብበት ወቅት ነው፡፡",
                        "A fixed 14-day fast (Nehase 1–14) commemorating the Dormition, Resurrection, and Assumption of the Virgin Mary; a season of prayer and intercession."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        m.put(FastingKeys.RAMADAN, new FastingInfo(
                new LocalizedText("ረመዳን", "Ramadan"),
                new LocalizedText(
                        "ረመዳን በሂጅራ አቆጣጠር ዘጠነኛው ወር እና በሙስሊሞች ዘንድ የጾም፣ ጸሎት ወር ነው",
                        "The Islamic month of fasting from dawn to sunset; a time of prayer, reflection, and charity."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.MUSLIM)
        ));

        m.put(FastingKeys.TSOME_DIHENET, new FastingInfo(
                new LocalizedText("ጾመ ድኅነት (ረቡእ·ዓርብ)", "Fast of Salvation (Wed & Fri)"),
                new LocalizedText(
                        "ከበአለ ሀምሳ ውጭ በሳምንት ረቡእ እና አርብ የምንፆመው ነው።",
                        "A fast observed on Wednesdays and Fridays outside of the 50 days after Easter."
                ),
                List.of(HolidayTags.RELIGIOUS, HolidayTags.CHRISTIAN)
        ));

        FASTING_INFO = Collections.unmodifiableMap(m);
    }
}
