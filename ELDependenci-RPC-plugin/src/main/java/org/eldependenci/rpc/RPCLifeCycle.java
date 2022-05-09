package org.eldependenci.rpc;

import com.ericlam.mc.eld.ELDLifeCycle;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;
import org.eldependenci.rpc.serve.ServeManager;

public class RPCLifeCycle implements ELDLifeCycle {

    @Inject
    private ServeManager serveManager;

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private RPCConfig config;


    @Override
    public void onEnable(JavaPlugin javaPlugin) {
        if (config.enabled) {
            scheduleService
                    .runAsync(javaPlugin, () -> serveManager.startServe())
                    .thenRunSync(v -> javaPlugin.getLogger().info("RPC Server started"))
                    .joinWithCatch(ex -> {
                        javaPlugin.getLogger().severe("Failed to start RPC server: " + ex.getMessage());
                        ex.printStackTrace();
                    });
        }
    }

    @Override
    public void onDisable(JavaPlugin javaPlugin) {
        serveManager.stop();
    }
}
