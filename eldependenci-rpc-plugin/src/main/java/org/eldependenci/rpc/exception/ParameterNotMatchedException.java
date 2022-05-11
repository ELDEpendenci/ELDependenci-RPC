package org.eldependenci.rpc.exception;

public class ParameterNotMatchedException extends ServiceException {

    private final int expectedLength;
    private final int actualLength;
    private final String methodName;
    private final String serviceName;


    public ParameterNotMatchedException(int expectedLength, int actualLength, String methodName, String serviceName) {
        super(
                String.format("Parameter of method %s in service %s is not matched: expected %s argument but got %s",
                        methodName, serviceName,
                        expectedLength,
                        actualLength
                )
        );

        this.expectedLength = expectedLength;
        this.actualLength = actualLength;
        this.methodName = methodName;
        this.serviceName = serviceName;
    }

    public int getActualLength() {
        return actualLength;
    }

    public int getExpectedLength() {
        return expectedLength;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
