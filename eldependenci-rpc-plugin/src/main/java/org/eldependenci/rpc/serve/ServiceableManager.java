package org.eldependenci.rpc.serve;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.eldependenci.rpc.protocol.RPCServiceable;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceableManager {

    private final Map<String, RPCServiceable> serviceableMap = new ConcurrentHashMap<>();

    @Inject
    private ServiceManager serviceManager;

    private final DebugLogger logger;



    @Inject
    public ServiceableManager(RPCConfig config,
                              @Named("eldrpc.protocols") Map<String, RPCProtocol> protocolMap,
                              Injector injector,
                              LoggingService loggingService) {
        protocolMap.forEach((name, protocol) -> {
            if (!config.enabledProtocols.contains(name)) return;
            RPCServiceable serviceable = injector.getInstance(protocol.serviceClass());
            serviceableMap.put(name, serviceable);
        });
        this.logger = loggingService.getLogger(ServiceableManager.class);
    }

    public CompletableFuture<Void> startAllServices() {
        List<CompletableFuture<Void>> promises = serviceableMap.entrySet().stream().map((entry) -> {
            var protocol = entry.getKey();
            var serviceable = entry.getValue();
            return CompletableFuture
                    .runAsync(() -> serviceable.StartService(serviceManager))
                    .thenRun(() -> logger.info(String.format("RPC 服務 %s 已啟動 (%s)", serviceable.getClass().getSimpleName(), protocol)));
        }).toList();
        return CompletableFuture.allOf(promises.toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> stopAllServices() {
        List<CompletableFuture<Void>> promises = serviceableMap.entrySet().stream().map((entry) -> {
            var protocol = entry.getKey();
            var serviceable = entry.getValue();
            return CompletableFuture
                    .runAsync(serviceable::StopService)
                    .thenRun(() -> logger.info(String.format("RPC 服務 %s 已停止 (%s)", serviceable.getClass().getSimpleName(), protocol)));
        }).toList();
        return CompletableFuture.allOf(promises.toArray(CompletableFuture[]::new));
    }
}
