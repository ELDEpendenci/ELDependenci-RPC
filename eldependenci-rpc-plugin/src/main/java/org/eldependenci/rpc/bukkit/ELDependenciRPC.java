package org.eldependenci.rpc.bukkit;

import com.ericlam.mc.eld.BukkitManagerProvider;
import com.ericlam.mc.eld.ELDBukkit;
import com.ericlam.mc.eld.ELDBukkitPlugin;
import com.ericlam.mc.eld.ServiceCollection;
import org.eldependenci.rpc.ELDRPCInstallation;
import org.eldependenci.rpc.ServiceCollectionBinder;
import org.eldependenci.rpc.bukkit.demo.BukkitDemoRemoteService;
import org.eldependenci.rpc.bukkit.demo.BukkitDemoService;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.implement.VersionGetter;


@ELDBukkit(
        lifeCycle = RPCBukkitLifeCycle.class,
        registry = RPCRegistry.class
)
public final class ELDependenciRPC extends ELDBukkitPlugin implements VersionGetter {

    private final ELDRPCInstallation eldrpcInstallation = new ELDRPCInstallation();

    @Override
    public void bindServices(ServiceCollection serviceCollection) {
        ServiceCollectionBinder.bind(serviceCollection, eldrpcInstallation, this);
    }

    @Override
    protected void manageProvider(BukkitManagerProvider bukkitManagerProvider) {
        var config = bukkitManagerProvider.getConfigStorage().getConfigAs(RPCConfig.class);
        if (config.enableDemo) {
            eldrpcInstallation.serves(BukkitDemoService.class);
            eldrpcInstallation.remotes(BukkitDemoRemoteService.class);
        }
    }

    @Override
    public String getVersion() {
        return this.getDescription().getVersion();
    }
}
