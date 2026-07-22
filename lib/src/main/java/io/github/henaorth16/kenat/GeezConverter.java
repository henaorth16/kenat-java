package io.github.henaorth16.kenat;

import io.github.henaorth16.kenat.exception.GeezConverterException;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts between Arabic numerals and Ge'ez (Ethiopic) numerals.
 * Mirrors {@code geezConverter.ts} from the TypeScript implementation.
 *
 * <p>Supports numbers 0–99 999 999.  All methods are static.
 *
 * <p>Symbol table:
 * <pre>
 *  ones:  ፩ ፪ ፫ ፬ ፭ ፮ ፯ ፰ ፱  (1–9)
 *  tens:  ፲ ፳ ፴ ፵ ፶ ፷ ፸ ፹ ፺  (10–90)
 *  100 :  ፻
 *  10 000: ፼
 * </pre>
 */
public final class GeezConverter {

    private GeezConverter() {}

    private static final String[] ONES = {"", "፩", "፪", "፫", "፬", "፭", "፮", "፯", "፰", "፱"};
    private static final String[] TENS = {"", "፲", "፳", "፴", "፵", "፶", "፷", "፸", "፹", "፺"};
    private static final String HUNDRED     = "፻";
    private static final String TEN_THOUSAND = "፼";

    // Reverse map: Ge'ez char → integer value
    private static final Map<String, Integer> REVERSE_MAP;
    static {
        REVERSE_MAP = new HashMap<>();
        for (int i = 1; i < ONES.length; i++) REVERSE_MAP.put(ONES[i], i);
        for (int i = 1; i < TENS.length; i++) REVERSE_MAP.put(TENS[i], i * 10);
        REVERSE_MAP.put(HUNDRED, 100);
        REVERSE_MAP.put(TEN_THOUSAND, 10_000);
    }

    // -----------------------------------------------------------------------
    // Arabic → Ge'ez
    // -----------------------------------------------------------------------

    /**
     * Converts a non-negative integer to its Ge'ez numeral string.
     *
     * @param input the number to convert (0 – 99 999 999)
     * @return Ge'ez numeral string; {@code "0"} for input 0
     * @throws GeezConverterException if {@code input} is negative or not an integer
     */
    public static String toGeez(int input) {
        if (input < 0) {
            throw new GeezConverterException("Input must be a non-negative integer.");
        }
        if (input == 0) return "0";
        return convertPositive(input);
    }

    /**
     * Converts a non-negative long to its Ge'ez numeral string.
     *
     * @param input the number to convert
     * @return Ge'ez numeral string
     * @throws GeezConverterException if {@code input} is negative
     */
    public static String toGeez(long input) {
        if (input < 0) throw new GeezConverterException("Input must be a non-negative integer.");
        if (input == 0) return "0";
        return convertPositive(input);
    }

    private static String convertBelow100(long n) {
        if (n <= 0) return "";
        int tensDigit = (int) (n / 10);
        int onesDigit = (int) (n % 10);
        return TENS[tensDigit] + ONES[onesDigit];
    }

    private static String convertPositive(long num) {
        if (num < 100) return convertBelow100(num);
        if (num == 100) return HUNDRED;
        if (num < 10_000) {
            long hundreds  = num / 100;
            long remainder = num % 100;
            String hundredPart = (hundreds > 1 ? convertBelow100(hundreds) : "") + HUNDRED;
            return hundredPart + convertBelow100(remainder);
        }
        // >= 10 000 — recursive
        long tenThousandPart = num / 10_000;
        long remainder = num % 10_000;
        String ttGeez = (tenThousandPart > 1 ? convertPositive(tenThousandPart) : "") + TEN_THOUSAND;
        return ttGeez + (remainder > 0 ? convertPositive(remainder) : "");
    }

    // -----------------------------------------------------------------------
    // Ge'ez → Arabic
    // -----------------------------------------------------------------------

    /**
     * Converts a Ge'ez numeral string to its Arabic integer equivalent.
     *
     * @param geezStr the Ge'ez numeral string
     * @return Arabic integer value; 0 for blank/empty input
     * @throws GeezConverterException if the string contains an unrecognised character
     */
    public static int toArabic(String geezStr) {
        if (geezStr == null || geezStr.isBlank()) return 0;

        int total         = 0;
        int currentNumber = 0;

        // Iterate over Unicode code points (each Ge'ez numeral is one code point but
        // may be represented as two Java chars — a supplementary-plane surrogate pair).
        int i = 0;
        while (i < geezStr.length()) {
            int cp = geezStr.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            i += Character.charCount(cp);

            Integer value = REVERSE_MAP.get(ch);
            if (value == null) {
                throw new GeezConverterException("Unknown Ge'ez numeral: " + ch);
            }

            if (value == 100 || value == 10_000) {
                currentNumber = (currentNumber == 0 ? 1 : currentNumber) * value;
                if (value == 10_000) {
                    total += currentNumber;
                    currentNumber = 0;
                }
            } else {
                currentNumber += value;
            }
        }
        return total + currentNumber;
    }
}
