package io.github.henaorth16.kenat.exception;

/**
 * Thrown when a Ge'ez numeral conversion fails (e.g. a negative number or an
 * unrecognised Ge'ez character sequence).
 *
 * <p>Mirrors the TypeScript {@code GeezConverterError}.
 */
public class GeezConverterException extends KenatException {

    /**
     * @param message description of the conversion failure
     */
    public GeezConverterException(String message) {
        super(message);
    }
}
