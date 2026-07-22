package io.github.henaorth16.kenat.exception;

/**
 * Thrown when a library function receives an argument of an incorrect type.
 *
 * <p>Mirrors the TypeScript {@code InvalidInputTypeError}.  In the Java port
 * this is typically thrown by the numeric-input validation helpers in
 * {@code EthiopianCalendarUtils} when a parameter is {@code null} or
 * otherwise not the expected primitive type.
 */
public class InvalidInputTypeException extends KenatException {

    private final String functionName;
    private final String parameterName;
    private final String expectedType;
    private final String receivedType;

    /**
     * @param functionName  name of the function where the bad argument was detected
     * @param parameterName name of the parameter
     * @param expectedType  description of the expected type (e.g. {@code "int"})
     * @param receivedType  description of the received type
     */
    public InvalidInputTypeException(
            String functionName,
            String parameterName,
            String expectedType,
            String receivedType) {
        super(String.format(
                "Invalid type for parameter '%s' in function '%s'. Expected '%s' but got '%s'.",
                parameterName, functionName, expectedType, receivedType));
        this.functionName  = functionName;
        this.parameterName = parameterName;
        this.expectedType  = expectedType;
        this.receivedType  = receivedType;
    }

    /** Name of the function where validation failed. */
    public String getFunctionName()  { return functionName;  }

    /** Name of the offending parameter. */
    public String getParameterName() { return parameterName; }

    /** Description of the expected type. */
    public String getExpectedType()  { return expectedType;  }

    /** Description of the received type. */
    public String getReceivedType()  { return receivedType;  }
}
