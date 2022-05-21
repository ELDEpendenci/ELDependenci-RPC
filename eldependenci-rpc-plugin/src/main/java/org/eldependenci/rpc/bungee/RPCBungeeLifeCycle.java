package org.eldependenci.rpc.bungee;

import com.ericlam.mc.eld.bungee.ELDLifeCycle;
import net.md_5.bungee.api.plugin.Plugin;
import org.eldependenci.rpc.RPCLifeCycle;

import java.util.logging.Logger;

public class RPCBungeeLifeCycle extends RPCLifeCycle<Plugin> implements ELDLifeCycle {

    @Override
    public Logger getLogger(Plugin plugin) {
        return plugin.getLogger();
    }
}
