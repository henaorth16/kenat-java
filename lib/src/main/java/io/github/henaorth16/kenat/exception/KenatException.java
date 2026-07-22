package io.github.henaorth16.kenat.exception;

/**
 * Base class for all Kenat library exceptions.
 *
 * <p>Mirrors the TypeScript {@code KenatError} base class.  All subclasses are
 * unchecked so callers are not forced to wrap every date operation in try/catch.
 *
 * <p>The {@link #getType()} method returns the simple class name, providing the
 * same structured discrimination as the TypeScript {@code toJSON().type} field.
 */
public class KenatException extends RuntimeException {

    /**
     * Constructs a {@code KenatException} with the given message.
     *
     * @param message human-readable description of the problem
     */
    public KenatException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code KenatException} wrapping another throwable.
     *
     * @param message human-readable description of the problem
     * @param cause   the underlying cause
     */
    public KenatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the simple class name of this exception, providing a stable
     * discriminator for callers that need to distinguish exception types
     * without {@code instanceof} checks.
     *
     * @return e.g. {@code "InvalidEthiopianDateException"}
     */
    public String getType() {
        return getClass().getSimpleName();
    }
}
