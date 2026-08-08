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

    /**
     * Builds an exception naming the runtime class of the offending argument.
     *
     * @param input the unrecognised argument (may be {@code null})
     * @return the exception, ready to throw
     */
    public static UnrecognizedInputException forInput(Object input) {
        return new UnrecognizedInputException(
                input == null ? "null" : input.getClass().getName());
    }
}
