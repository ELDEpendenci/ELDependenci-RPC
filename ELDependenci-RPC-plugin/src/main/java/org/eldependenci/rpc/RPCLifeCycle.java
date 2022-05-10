package org.eldependenci.rpc;

import com.ericlam.mc.eld.ELDLifeCycle;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;
import org.eldependenci.rpc.serve.ServiceableManager;

public class RPCLifeCycle implements ELDLifeCycle {


    @Inject
    private ServiceableManager serviceableManager;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private RPCConfig config;


    @Override
    public void onEnable(JavaPlugin javaPlugin) {
        if (config.enabled) {
            serviceableManager.startAllServices()
                    .thenRunSync(v -> javaPlugin.getLogger().info("All RPC Services Started."))
                    .joinWithCatch(ex -> {
                        javaPlugin.getLogger().severe("Failed to start RPC services: " + ex.getMessage());
                        ex.printStackTrace();
                    });
        }
    }

    @Override
    public void onDisable(JavaPlugin javaPlugin) {
        try {
            serviceableManager.stopAllServices().block();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
