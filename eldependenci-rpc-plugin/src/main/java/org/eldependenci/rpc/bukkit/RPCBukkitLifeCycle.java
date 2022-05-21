package org.eldependenci.rpc.bukkit;

import com.ericlam.mc.eld.bukkit.ELDLifeCycle;
import org.bukkit.plugin.java.JavaPlugin;
import org.eldependenci.rpc.RPCLifeCycle;

import java.util.logging.Logger;

public class RPCBukkitLifeCycle extends RPCLifeCycle<JavaPlugin> implements ELDLifeCycle {

    @Override
    public Logger getLogger(JavaPlugin javaPlugin) {
        return javaPlugin.getLogger();
    }

}
