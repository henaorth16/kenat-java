package io.github.henaorth16.kenat.exception;

/**
 * Thrown when invalid configuration options are passed to the
 * {@code MonthGrid} builder.
 *
 * <p>Mirrors the TypeScript {@code InvalidGridConfigError}.
 */
public class InvalidGridConfigException extends KenatException {

    /**
     * @param message description of the configuration problem
     */
    public InvalidGridConfigException(String message) {
        super(message);
    }
}
