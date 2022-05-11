package org.eldependenci.rpc.exception;

public class MethodNotFoundException extends ServiceException {

    private final String methodName;
    private final String serviceName;

    public MethodNotFoundException(String methodName, String serviceName) {
        super(String.format("Method %s not found in service %s", methodName, serviceName));
        this.methodName = methodName;
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
