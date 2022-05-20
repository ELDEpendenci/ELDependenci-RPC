package org.eldependenci.rpc;

import com.ericlam.mc.eld.LifeCycle;
import com.google.inject.Inject;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.serve.ServiceableManager;

import java.util.logging.Logger;

public abstract class RPCLifeCycle<Plugin> implements LifeCycle<Plugin> {
    @Inject
    private ServiceableManager serviceableManager;

    @Inject
    private RPCConfig config;

    @Override
    public void onEnable(Plugin plugin) {
        if (config.enabled) {
            serviceableManager.startAllServices()
                    .thenRun(() -> getLogger(plugin).info("All RPC Services Started."))
                    .whenComplete((V, ex) -> {
                        if (ex != null) {
                            getLogger(plugin).severe("Failed to start RPC services: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void onDisable(Plugin plugin) {
        if (config.enabled) {
            serviceableManager.stopAllServices().join();
        }
    }

    public abstract Logger getLogger(Plugin plugin);
}
