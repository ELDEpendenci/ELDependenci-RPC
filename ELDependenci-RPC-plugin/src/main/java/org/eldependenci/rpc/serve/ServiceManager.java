package org.eldependenci.rpc.serve;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.eldependenci.rpc.JsonMapperFactory;
import org.eldependenci.rpc.annotation.DoAsync;
import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.protocol.ServiceHandler;
import org.eldependenci.rpc.exception.*;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceManager implements ServiceHandler {

    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private final Map<String, Method> serviceMethodCache = new ConcurrentHashMap<>();

    private final ObjectMapper mapper;

    private final DebugLogger logger;

    @Inject
    public ServiceManager(
            Injector injector,
            @Named("eldrpc.serves") Set<Class<?>> serves,
            LoggingService loggingService,
            JsonMapperFactory factory
    ) {
        this.logger = loggingService.getLogger(ServiceManager.class);
        serves.forEach(serve -> this.serviceMap.put(serve.getSimpleName(), injector.getInstance(serve)));
        this.mapper = factory.jsonMapper();
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(String name) throws ServiceNotFoundException {
        return Optional.ofNullable((T) serviceMap.get(name)).orElseThrow(() -> new ServiceNotFoundException(name));
    }


    public Method getServiceMethod(Object obj, String name) throws MethodNotFoundException {
        Class<?> service = obj.getClass();
        var key = String.format("%s.%s", service.getSimpleName(), name);
        if (serviceMethodCache.containsKey(key)) {
            return serviceMethodCache.get(key);
        }
        for (Method method : service.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.canAccess(obj)) {
                this.serviceMethodCache.put(key, method);
                return method;
            }
        }
        throw new MethodNotFoundException(name, service.getSimpleName());
    }


    public Object invokeServiceMethod(Object instance, Method method, Object[] args) throws Exception {

        if (method.getParameterCount() != args.length) {
            throw new ParameterNotMatchedException(method.getParameterCount(), args.length, method.getName(), instance.getClass().getSimpleName());
        }

        Object[] invokes = new Object[method.getParameterCount()];

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            Object value = args[i];
            var javaType = mapper.getTypeFactory().constructType(parameter.getParameterizedType());
            Object pass = mapper.convertValue(value, javaType);
            invokes[i] = pass;
        }

        return method.invoke(instance, invokes);
    }


    public Object validateReturned(Object returned, Type type) throws ReturnTypeNotMatchedException {
        var javaType = mapper.constructType(type);
        if (!javaType.isTypeOrSuperTypeOf(returned.getClass())) {
            throw new ReturnTypeNotMatchedException(type.getTypeName(), returned.getClass().getName(), returned);
        }
        return mapper.convertValue(returned, javaType);
    }

    @Override
    public Response invokes(RPCPayload payload) throws Exception {
        var service = getService(payload.service());
        var method = getServiceMethod(service, payload.method());
        var args = method.getParameterCount() == 0 ? new Object[0] : payload.parameters();
        var returned = invokeServiceMethod(service, method, args);
        return new Response(returned, method.getGenericReturnType());
    }

    @Override
    public Object finalizeType(Object result, Type returnType) throws Exception {
        return validateReturned(result, returnType);
    }

    @Override
    public boolean shouldCallAsync(RPCPayload payload) throws Exception {
        var service = getService(payload.service());
        var method = getServiceMethod(service, payload.method());
        return method.isAnnotationPresent(DoAsync.class);
    }

    @Override
    public RPCError toRPCError(Exception e, boolean debug) {
        if (e instanceof ServiceException) {
            logger.debug("Resolved Exception: {0} => {1}", e.getClass().getSimpleName(), e.getMessage());
        } else {
            logger.warn("Resolved Exception: {0} => {1} ", e.getClass().getSimpleName(), e.getMessage());
            logger.debug(e);
        }
        var code = e instanceof ServiceException ? 400 : 500;
        return new RPCError(code, e.getMessage(), debug ? Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new) : new String[0]);
    }
}
