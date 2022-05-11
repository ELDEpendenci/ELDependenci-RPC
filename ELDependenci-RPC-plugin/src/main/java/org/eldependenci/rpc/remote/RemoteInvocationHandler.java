package org.eldependenci.rpc.remote;

import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.protocol.RPCRequester;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RemoteInvocationHandler implements InvocationHandler {

    private final RPCRequester requester;

    private final RequesterManager requesterManager;

    private final String serviceName;

    public RemoteInvocationHandler(
            Class<?> service,
            RequesterManager requesterManager
    ) {
        this.requesterManager = requesterManager;
        this.requester = requesterManager.getRequester(service);
        this.serviceName = requesterManager.findInfo(service).map(RPCInfo::serviceName).orElse(service.getSimpleName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        } else {
            args = args != null ? args : new String[0];
            var id = System.nanoTime();
            var payload = new RPCPayload(id, method.getName(), serviceName,  args);
            var future = this.requester.offerRequest(payload);
            return requesterManager.handleFuture(future, method.getGenericReturnType());
        }
    }

}
