package org.eldependenci.rpc.remote;

import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.context.RPCPayload;

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

    @Inject
    private RPCConfig config;

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

    public <T> T createServiceDynamically(Class<T> service, RPCInfo info) {
        var serviceName = Optional.ofNullable(info.serviceName()).orElse(service.getSimpleName());
        var requester = requestManager.getRequesterDynamically(info);
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class[]{service},
                (proxy1, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(this, args);
                    } else {
                        args = args != null ? args : new String[0];
                        var id = System.nanoTime();
                        var payload = new RPCPayload(id, method.getName(), serviceName, args, info.authToken());
                        var future = requester.offerRequest(payload);
                        return this.requestManager.handleFuture(future, method.getGenericReturnType());
                    }
                }
        );
    }


    public void preloadAllServices(List<Class<?>> services) {
        services.forEach(this::createService);
    }

}
