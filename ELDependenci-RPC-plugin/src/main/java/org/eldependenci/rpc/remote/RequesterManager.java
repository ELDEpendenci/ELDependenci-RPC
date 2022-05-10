package org.eldependenci.rpc.remote;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.JsonMapperFactory;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RequesterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequesterManager.class);
    private final ObjectMapper mapper;
    private final DebugLogger logger;

    private final Map<String, RPCRequester> requesterMap = new ConcurrentHashMap<>();


    @Inject
    private ScheduleService scheduleService;

    @Inject
    private ELDependenciRPC plugin;

    @Inject
    public RequesterManager(Injector injector, @Named("eldrpc.protocols") Map<String, RPCProtocol> protocolMap, JsonMapperFactory factory, LoggingService loggingService) {
        this.mapper = factory.jsonMapper();
        this.logger = loggingService.getLogger(RequesterManager.class);
        protocolMap.forEach((protocol, rpc) -> this.requesterMap.put(protocol, injector.getInstance(rpc.requester())));
    }

    public RPCRequester getByProtocol(String protocol) {
        return Optional.ofNullable(this.requesterMap.get(protocol)).orElseThrow(() -> new IllegalArgumentException("Protocol " + protocol + " is not supported"));
    }


    public Object handleFuture(CompletableFuture<?> future, Type returnType) throws Exception{

        var jt = this.mapper.getTypeFactory().constructType(returnType);
        var realFuture = future.thenApply(r -> this.mapper.convertValue(r, jt));
        var cls = (Class<?>) returnType;

        if (cls.isAssignableFrom(CompletableFuture.class)) {
            realFuture = future.thenApply(r -> this.mapper.convertValue(r, jt.getBindings().getBoundType(0)));
            return realFuture;
        } else if (cls.isAssignableFrom(ScheduleService.BukkitPromise.class)) {
            realFuture = future.thenApply(r -> this.mapper.convertValue(r, jt.getBindings().getBoundType(0)));
            return scheduleService.callAsync(plugin, realFuture::join);
        } else {
            return realFuture.join();
        }
    }

}
