package org.eldependenci.rpc.exception;

public class ReturnTypeNotMatchedException extends ServiceException {

    private final String expectedType;
    private final String actualType;

    private final Object value;

    public ReturnTypeNotMatchedException(String expectedType, String actualType, Object o) {
        super(String.format("Cannot handle return object (%s), expected type %s but got %s", o.toString(), expectedType, actualType));
        this.expectedType = expectedType;
        this.actualType = actualType;
        this.value = o;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public String getActualType() {
        return actualType;
    }

    public Object getValue() {
        return value;
    }
}
