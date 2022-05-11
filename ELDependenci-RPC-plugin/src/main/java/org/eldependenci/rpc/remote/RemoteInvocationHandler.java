package org.eldependenci.rpc.remote;

import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.protocol.RPCRequester;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

public class RemoteInvocationHandler implements InvocationHandler {

    private final RPCRequester requester;

    private final RequesterManager requesterManager;

    private final String serviceName;
    private final String token;

    public RemoteInvocationHandler(
            Class<?> service,
            RequesterManager requesterManager,
            RPCConfig config
    ) {
        this.requesterManager = requesterManager;
        this.requester = requesterManager.getRequester(service);
        this.serviceName = requesterManager.findInfo(service).map(RPCInfo::serviceName).orElse(service.getSimpleName());
        this.token = Optional.ofNullable(config.token).map(s -> s.isBlank() ? null : s).orElseGet(() -> System.getenv("RPC_TOKEN"));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else {
            args = args != null ? args : new String[0];
            var id = System.nanoTime();
            var payload = new RPCPayload(id, method.getName(), serviceName,  args, token);
            var future = this.requester.offerRequest(payload);
            return requesterManager.handleFuture(future, method.getGenericReturnType());
        }
    }

}
