package io.github.henaorth16.kenat.exception;

/**
 * Thrown when the {@code Kenat} constructor receives an argument type it cannot
 * handle.
 *
 * <p>Mirrors the TypeScript {@code UnrecognizedInputError}.
 */
public class UnrecognizedInputException extends KenatException {

    private final String inputType;

    /**
     * @param inputType a description of the unrecognised type (e.g. the class name)
     */
    public UnrecognizedInputException(String inputType) {
        super("Unrecognized input type for Kenat constructor: " + inputType);
        this.inputType = inputType;
    }

    /** Description of the unrecognised input type. */
    public String getInputType() {
        return inputType;
    }
}
