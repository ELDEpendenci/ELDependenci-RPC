package org.eldependenci.rpc.remote;

import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RemoteInvocationHandler implements InvocationHandler {

    private final RPCRequester requester;
    private final RPCClient client;

    private final RequesterManager requesterManager;

    public RemoteInvocationHandler(
            Class<?> service,
            RequesterManager requesterManager
    ) {
        var rpc = service.getAnnotation(RPCClient.class);

        if (rpc == null) {
            throw new IllegalArgumentException("Class is not annotated with @RPCClient");
        }

        this.requesterManager = requesterManager;
        this.client = rpc;
        this.requester = requesterManager.getByProtocol(rpc.protocol());

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else {
            args = args != null ? args : new String[0];
            var payload = new RPCPayload(method.getDeclaringClass().getSimpleName(), method.getName(), args);
            var future = this.requester.offerRequest(payload, client);
            return requesterManager.handleFuture(future, method.getGenericReturnType());
        }
    }

}
