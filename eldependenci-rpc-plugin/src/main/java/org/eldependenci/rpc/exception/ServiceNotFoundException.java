package org.eldependenci.rpc.exception;

public class ServiceNotFoundException extends ServiceException {

    private final String serviceName;

    public ServiceNotFoundException(String serviceName) {
        super("Service not found: " + serviceName);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}
