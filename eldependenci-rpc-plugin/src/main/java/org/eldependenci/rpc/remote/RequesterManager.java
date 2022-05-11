package org.eldependenci.rpc.remote;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.JsonMapperFactory;
import org.eldependenci.rpc.config.RPCRemoteConfig;
import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RequesterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequesterManager.class);
    private final ObjectMapper mapper;
    private final DebugLogger logger;

    private final Map<String, Class<? extends RPCRequester>> requesterMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Class<?>, RPCRequester>> instanceMap = new ConcurrentHashMap<>();

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private ELDependenciRPC plugin;

    @Inject
    private Injector injector;

    @Inject
    private RPCRemoteConfig remoteConfig;

    @Inject
    public RequesterManager(@Named("eldrpc.protocols") Map<String, RPCProtocol> protocolMap, JsonMapperFactory factory, LoggingService loggingService) {
        this.mapper = factory.jsonMapper();
        this.logger = loggingService.getLogger(RequesterManager.class);
        protocolMap.forEach((protocol, rpc) -> {
            this.requesterMap.put(protocol, rpc.requester());
            this.instanceMap.put(protocol, new ConcurrentHashMap<>());
        });
    }

    public Optional<RPCInfo> findInfo(Class<?> service) {
        return remoteConfig.remotes
                .stream()
                .filter(r -> r.locate().equals(service.getName()))
                .findAny();
    }

    public CompletableFuture<RPCRequester> getRequesterDynamically(RPCInfo info) {
        var requester = this.requesterMap.get(info.protocol());
        if (requester == null) {
            throw new IllegalArgumentException("Protocol " + info.protocol() + " is not supported");
        }
        var ins = injector.getInstance(requester);
        return ins.initialize(info).thenApply(v -> ins);
    }

    public CompletableFuture<RPCRequester> getRequester(Class<?> service) {

        var info = findInfo(service).orElseThrow(() -> new IllegalStateException("Can't find remote config for " + service.getName() + ", have you defined it on remotes.yml?"));

        var protocol = info.protocol();

        var instances = this.instanceMap.get(protocol);
        if (instances == null) {
            throw new IllegalArgumentException("Protocol " + protocol + " is not supported");
        }

        var ins = instances.get(service);
        if (ins != null){
            return CompletableFuture.completedFuture(ins);
        }

        var requester = this.requesterMap.get(protocol);
        var newIns = injector.getInstance(requester);
        return newIns.initialize(info).thenApply(v -> {
            instances.put(service, newIns);
            return newIns;
        });
    }


    public Object handleFuture(CompletableFuture<?> future, Type returnType) throws Exception {

        var jt = this.mapper.getTypeFactory().constructType(returnType);
        var realFuture = future.thenApply(r -> {

            if (returnType == Void.TYPE) {
                return null;
            }

            return this.mapper.convertValue(r, jt);
        });

        if (jt.isTypeOrSubTypeOf(CompletableFuture.class)) {
            realFuture = future.thenApply(r -> this.mapper.convertValue(r, jt.getBindings().getBoundType(0)));
            return realFuture;
        } else if (jt.isTypeOrSubTypeOf(ScheduleService.BukkitPromise.class)) {
            realFuture = future.thenApply(r -> this.mapper.convertValue(r, jt.getBindings().getBoundType(0)));
            return scheduleService.callAsync(plugin, realFuture::join);
        } else {
            return realFuture.join();
        }
    }

}
