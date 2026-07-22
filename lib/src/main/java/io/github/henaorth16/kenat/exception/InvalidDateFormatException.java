package io.github.henaorth16.kenat.exception;

/**
 * Thrown when a date string passed to the {@code Kenat} constructor or parser
 * does not match the expected {@code yyyy/mm/dd} or {@code yyyy-mm-dd} format.
 *
 * <p>Mirrors the TypeScript {@code InvalidDateFormatError}.
 */
public class InvalidDateFormatException extends KenatException {

    private final String inputString;

    /**
     * @param inputString the string that could not be parsed
     */
    public InvalidDateFormatException(String inputString) {
        super(String.format(
                "Invalid date string format: \"%s\". Expected 'yyyy/mm/dd' or 'yyyy-mm-dd'.",
                inputString));
        this.inputString = inputString;
    }

    /** The raw string that failed to parse. */
    public String getInputString() {
        return inputString;
    }
}
