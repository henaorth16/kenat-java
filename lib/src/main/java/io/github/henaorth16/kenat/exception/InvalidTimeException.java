package io.github.henaorth16.kenat.exception;

/**
 * Thrown when invalid hour, minute, or period values are provided to the
 * Ethiopian time API.
 *
 * <p>Mirrors the TypeScript {@code InvalidTimeError}.
 */
public class InvalidTimeException extends KenatException {

    /**
     * @param message description of the time validation failure
     */
    public InvalidTimeException(String message) {
        super(message);
    }
}
