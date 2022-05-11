package org.eldependenci.rpc.serve;

import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.protocol.RPCServiceable;
import org.eldependenci.rpc.protocol.RPCProtocol;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceableManager {

    private final Map<String, RPCServiceable> serviceableMap = new ConcurrentHashMap<>();

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private ELDependenciRPC plugin;

    @Inject
    private ServiceManager serviceManager;


    @Inject
    public ServiceableManager(RPCConfig config, @Named("eldrpc.protocols") Map<String, RPCProtocol> protocolMap, Injector injector) {
        protocolMap.forEach((name, protocol) -> {
            if (!config.enabledProtocols.contains(name)) return;
            RPCServiceable serviceable = injector.getInstance(protocol.serviceClass());
            serviceableMap.put(name, serviceable);
        });
    }

    public ScheduleService.BukkitPromise<Void> startAllServices() {
        List<ScheduleService.BukkitPromise<Void>> promises = serviceableMap.entrySet().stream().map((entry) -> {
            var protocol = entry.getKey();
            var serviceable = entry.getValue();
            return scheduleService
                    .runAsync(plugin, () -> serviceable.StartService(serviceManager))
                    .thenRunSync(v -> plugin.getLogger().info(String.format("RPC 服務 %s 已啟動 (%s)", serviceable.getClass().getSimpleName(), protocol)));
        }).toList();
        return scheduleService.runAllAsync(plugin, promises);
    }

    public ScheduleService.BukkitPromise<Void> stopAllServices() {
        List<ScheduleService.BukkitPromise<Void>> promises = serviceableMap.entrySet().stream().map((entry) -> {
            var protocol = entry.getKey();
            var serviceable = entry.getValue();
            return scheduleService
                    .runAsync(plugin, serviceable::StopService)
                    .thenRunSync(v -> plugin.getLogger().info(String.format("RPC 服務 %s 已停止 (%s)", serviceable.getClass().getSimpleName(), protocol)));
        }).toList();
        return scheduleService.runAllAsync(plugin, promises);
    }
}
