package org.eldependenci.rpc;

import com.ericlam.mc.eld.AddonInstallation;
import com.ericlam.mc.eld.ManagerProvider;
import com.ericlam.mc.eld.ServiceCollection;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.config.RPCRemoteConfig;
import org.eldependenci.rpc.bungee.demo.DemoRemoteService;
import org.eldependenci.rpc.bungee.demo.DemoService;
import org.eldependenci.rpc.implement.JavalinServiceable;
import org.eldependenci.rpc.implement.OkHttpRequester;
import org.eldependenci.rpc.implement.OkWebSocketRequester;
import org.eldependenci.rpc.implement.VersionGetter;
import org.eldependenci.rpc.remote.RemoteManager;
import org.eldependenci.rpc.remote.RequesterManager;
import org.eldependenci.rpc.retrofit.RetrofitManager;
import org.eldependenci.rpc.serve.ServiceManager;
import org.eldependenci.rpc.serve.ServiceableManager;

public class ServiceCollectionBinder {

    public static void bind(ServiceCollection serviceCollection, ELDRPCInstallation eldrpcInstallation, VersionGetter getter) {
        serviceCollection.addConfiguration(RPCConfig.class);
        serviceCollection.addConfiguration(RPCRemoteConfig.class);

        serviceCollection.addSingleton(RetrofitManager.class);

        serviceCollection.addSingleton(ServiceableManager.class);
        serviceCollection.addSingleton(ServiceManager.class);

        serviceCollection.addSingleton(JsonMapperFactory.class);

        serviceCollection.addSingleton(RemoteManager.class);
        serviceCollection.addSingleton(RequesterManager.class);

        AddonInstallation installer = serviceCollection.getInstallation(AddonInstallation.class);
        installer.customInstallation(RPCInstallation.class, eldrpcInstallation);
        installer.installModule(new RPCModule(eldrpcInstallation, getter));

        eldrpcInstallation.registerProtocol("HTTP", JavalinServiceable.class, OkHttpRequester.class);
        eldrpcInstallation.registerProtocol("WEB_SOCKET", JavalinServiceable.class, OkWebSocketRequester.class);
    }
}
