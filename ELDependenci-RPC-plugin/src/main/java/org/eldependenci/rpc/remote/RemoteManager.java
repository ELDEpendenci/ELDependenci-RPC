package org.eldependenci.rpc.remote;

import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class RemoteManager {

    private final Map<Class<?>, Object> proxyMap = new ConcurrentHashMap<>();

    @Inject
    private RequesterManager requestManager;

    public <T> T getRemoteService(Class<T> service) {
        return (T) Optional.ofNullable(proxyMap.get(service)).orElseGet(() -> createService(service));
    }


    private <T> T createService(Class<T> service) {
        var proxy = Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class[]{service},
                new RemoteInvocationHandler(service, requestManager)
        );
        proxyMap.put(service, proxy);
        return (T) proxy;
    }


    public void preloadAllServices(List<Class<?>> services){
        services.forEach(this::createService);
    }

}
