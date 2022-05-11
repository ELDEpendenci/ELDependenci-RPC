package org.eldependenci.rpc;

import com.ericlam.mc.eld.AddonManager;
import com.ericlam.mc.eld.ELDBukkitAddon;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.annotations.ELDPlugin;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.config.RPCRemoteConfig;
import org.eldependenci.rpc.demo.DemoRemoteService;
import org.eldependenci.rpc.demo.DemoService;
import org.eldependenci.rpc.implement.JavalinServiceable;
import org.eldependenci.rpc.implement.OkHttpRequester;
import org.eldependenci.rpc.implement.OkWebSocketRequester;
import org.eldependenci.rpc.remote.RemoteManager;
import org.eldependenci.rpc.remote.RequesterManager;
import org.eldependenci.rpc.retrofit.RetrofitManager;
import org.eldependenci.rpc.serve.ServiceManager;
import org.eldependenci.rpc.serve.ServiceableManager;


@ELDPlugin(
        lifeCycle = RPCLifeCycle.class,
        registry = RPCRegistry.class
)
public final class ELDependenciRPC extends ELDBukkitAddon {

    @Override
    protected void bindServices(ServiceCollection serviceCollection) {

        serviceCollection.addConfiguration(RPCConfig.class);
        serviceCollection.addConfiguration(RPCRemoteConfig.class);

        serviceCollection.addSingleton(RetrofitManager.class);

        serviceCollection.addSingleton(ServiceableManager.class);
        serviceCollection.addSingleton(ServiceManager.class);

        serviceCollection.addSingleton(JsonMapperFactory.class);

        serviceCollection.addSingleton(RemoteManager.class);
        serviceCollection.addSingleton(RequesterManager.class);


    }

    @Override
    protected void preAddonInstall(ManagerProvider provider, AddonManager installer) {
        ELDRPCInstallation eldrpcInstallation = new ELDRPCInstallation();
        var config = provider.getConfigStorage().getConfigAs(RPCConfig.class);

        if (config.enableDemo) {
            eldrpcInstallation.serves(DemoService.class);
            eldrpcInstallation.remotes(DemoRemoteService.class);
        }

        eldrpcInstallation.registerProtocol("HTTP", JavalinServiceable.class, OkHttpRequester.class);
        eldrpcInstallation.registerProtocol("WEB_SOCKET", JavalinServiceable.class, OkWebSocketRequester.class);

        installer.customInstallation(RPCInstallation.class, eldrpcInstallation);
        installer.installModule(new RPCModule(eldrpcInstallation));
    }
}
