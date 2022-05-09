package org.eldependenci.rpc.serve;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.eldependenci.rpc.exception.MethodNotFoundException;
import org.eldependenci.rpc.exception.ParameterNotMatchedException;
import org.eldependenci.rpc.exception.ReturnTypeNotMatchedException;
import org.eldependenci.rpc.exception.ServiceNotFoundException;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceManager {

    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private final Map<String, Method> serviceMethodCache = new ConcurrentHashMap<>();

    @Inject
    private ObjectMapper mapper;

    private final DebugLogger logger;

    @Inject
    public ServiceManager(Injector injector, @Named("eldrpc.serves") Set<Class<?>> serves, LoggingService loggingService) {
        this.logger = loggingService.getLogger(ServiceManager.class);
        serves.forEach(serve -> this.serviceMap.put(serve.getSimpleName(), injector.getInstance(serve)));
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
            logger.debug("parsing {0} to {1}", value.getClass(), parameter.getParameterizedType());
            Object pass = mapper.convertValue(value, javaType);
            invokes[i] = pass;
        }

        return method.invoke(instance, invokes);
    }


    public <T> T validateReturned(Object returned, Class<T> type) throws ReturnTypeNotMatchedException {
        if (!type.isAssignableFrom(returned.getClass())) {
            throw new ReturnTypeNotMatchedException(type.getTypeName(), returned.getClass().getName(), returned);
        }
        logger.debug("belongs to ConfigurationSerializable: {0}", returned instanceof ConfigurationSerializable);
        logger.debug("serializing {0}", returned.getClass());
        return mapper.convertValue(returned, type);
    }

    public Object noValidateReturned(Object returned, Type type) {
        return returned;
    }

}
