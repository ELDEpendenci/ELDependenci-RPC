package org.eldependenci.rpc.bungee;

import com.ericlam.mc.eld.BungeeManageProvider;
import com.ericlam.mc.eld.ELDBungee;
import com.ericlam.mc.eld.ELDBungeePlugin;
import com.ericlam.mc.eld.ServiceCollection;
import org.eldependenci.rpc.ELDRPCInstallation;
import org.eldependenci.rpc.ServiceCollectionBinder;
import org.eldependenci.rpc.bungee.demo.DemoRemoteService;
import org.eldependenci.rpc.bungee.demo.DemoService;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.implement.VersionGetter;

@ELDBungee(
        registry = RPCRegistry.class,
        lifeCycle = RPCBungeeLifeCycle.class
)
public class ELDependenciRPC extends ELDBungeePlugin implements VersionGetter {

    private final ELDRPCInstallation eldrpcInstallation = new ELDRPCInstallation();


    @Override
    protected void manageProvider(BungeeManageProvider bungeeManageProvider) {
        var config = bungeeManageProvider.getConfigStorage().getConfigAs(RPCConfig.class);
        if (config.enableDemo) {
            eldrpcInstallation.serves(DemoService.class);
            eldrpcInstallation.remotes(DemoRemoteService.class);
        }
    }

    @Override
    public void bindServices(ServiceCollection serviceCollection) {
        ServiceCollectionBinder.bind(serviceCollection, eldrpcInstallation, this);
    }


    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }
}
