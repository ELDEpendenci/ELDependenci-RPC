package org.eldependenci.rpc;

import com.ericlam.mc.eld.AddonManager;
import com.ericlam.mc.eld.ELDBukkitAddon;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import org.eldependenci.rpc.demo.DemoService;
import org.eldependenci.rpc.retrofit.RetrofitManager;
import org.eldependenci.rpc.serve.ServeManager;
import org.eldependenci.rpc.serve.ServiceManager;


@ELDPlugin(
        lifeCycle = RPCLifeCycle.class,
        registry = RPCRegistry.class
)
public final class ELDependenciRPC extends ELDBukkitAddon {

    @Override
    protected void bindServices(ServiceCollection serviceCollection) {
        serviceCollection.addConfiguration(RPCConfig.class);
        serviceCollection.addSingleton(RetrofitManager.class);
        serviceCollection.addSingleton(ServeManager.class);
        serviceCollection.addSingleton(ServiceManager.class);
    }

    @Override
    protected void preAddonInstall(ManagerProvider provider, AddonManager installer) {
        ELDRPCInstallation eldrpcInstallation = new ELDRPCInstallation();
        var config = provider.getConfigStorage().getConfigAs(RPCConfig.class);

        if (config.enableDemo) {
            eldrpcInstallation.serves(DemoService.class);
        }

        installer.customInstallation(RPCInstallation.class, eldrpcInstallation);
        installer.installModule(new RPCModule(eldrpcInstallation));
    }
}
