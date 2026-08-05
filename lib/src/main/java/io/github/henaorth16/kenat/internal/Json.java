package io.github.henaorth16.kenat.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON reader.
 *
 * <p>Kenat ships no runtime dependencies, so the bundled data files
 * (currently {@code kenat/nigs.json}) are parsed with this reader rather than
 * with Jackson or Gson.  It handles the whole JSON grammar needed by those
 * resources: objects, arrays, strings (including {@code \\u} escapes), numbers,
 * booleans and {@code null}.
 *
 * <p>Values map to Java as follows:
 * <ul>
 *   <li>object → {@link LinkedHashMap} (insertion-ordered)</li>
 *   <li>array → {@link ArrayList}</li>
 *   <li>string → {@link String}</li>
 *   <li>number → {@link Long} when integral, otherwise {@link Double}</li>
 *   <li>{@code true}/{@code false} → {@link Boolean}</li>
 *   <li>{@code null} → {@code null}</li>
 * </ul>
 *
 * <p>This class is internal API and may change without notice.
 */
public final class Json {

    private final String src;
    private int pos;

    private Json(String src) {
        this.src = src;
    }

    /**
     * Parses a complete JSON document.
     *
     * @param text the JSON text
     * @return the parsed value (see class docs for the type mapping)
     * @throws JsonException if the text is not well-formed JSON
     */
    public static Object parse(String text) {
        if (text == null) throw new JsonException("JSON input must not be null");
        Json p = new Json(text);
        p.skipWhitespace();
        Object value = p.readValue();
        p.skipWhitespace();
        if (p.pos < p.src.length()) {
            throw p.error("Trailing content after JSON value");
        }
        return value;
    }

    /**
     * Parses a JSON document that is expected to be an object.
     *
     * @param text the JSON text
     * @return the parsed object
     * @throws JsonException if the text is not a well-formed JSON object
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object value = parse(text);
        if (!(value instanceof Map)) {
            throw new JsonException("Expected a JSON object at the document root");
        }
        return (Map<String, Object>) value;
    }

    // -----------------------------------------------------------------------
    // Grammar
    // -----------------------------------------------------------------------

    private Object readValue() {
        if (pos >= src.length()) throw error("Unexpected end of input");
        char c = src.charAt(pos);
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default  -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            map.put(key, readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == '}') { pos++; return map; }
            throw error("Expected ',' or '}' in object");
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            skipWhitespace();
            list.add(readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == ']') { pos++; return list; }
            throw error("Expected ',' or ']' in array");
        }
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= src.length()) throw error("Unterminated string");
            char c = src.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }

            if (pos >= src.length()) throw error("Unterminated escape sequence");
            char esc = src.charAt(pos++);
            switch (esc) {
                case '"'  -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/'  -> sb.append('/');
                case 'b'  -> sb.append('\b');
                case 'f'  -> sb.append('\f');
                case 'n'  -> sb.append('\n');
                case 'r'  -> sb.append('\r');
                case 't'  -> sb.append('\t');
                case 'u'  -> {
                    if (pos + 4 > src.length()) throw error("Truncated \\u escape");
                    String hex = src.substring(pos, pos + 4);
                    pos += 4;
                    try {
                        sb.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException e) {
                        throw error("Invalid \\u escape: \\u" + hex);
                    }
                }
                default -> throw error("Invalid escape character: \\" + esc);
            }
        }
    }

    private Object readNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        boolean integral = true;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c >= '0' && c <= '9') { pos++; continue; }
            if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                integral = false;
                pos++;
                continue;
            }
            break;
        }
        String text = src.substring(start, pos);
        if (text.isEmpty() || text.equals("-")) throw error("Invalid number");
        try {
            return integral ? (Object) Long.parseLong(text) : (Object) Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw error("Invalid number: " + text);
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!src.startsWith(literal, pos)) throw error("Invalid literal, expected " + literal);
        pos += literal.length();
        return value;
    }

    // -----------------------------------------------------------------------
    // Lexing helpers
    // -----------------------------------------------------------------------

    private void skipWhitespace() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    private char peek() {
        if (pos >= src.length()) throw error("Unexpected end of input");
        return src.charAt(pos);
    }

    private void expect(char expected) {
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw error("Expected '" + expected + "'");
        }
        pos++;
    }

    private JsonException error(String message) {
        return new JsonException(message + " at offset " + pos);
    }

    /** Thrown when JSON text cannot be parsed. */
    public static final class JsonException extends RuntimeException {
        JsonException(String message) {
            super(message);
        }
    }
}
