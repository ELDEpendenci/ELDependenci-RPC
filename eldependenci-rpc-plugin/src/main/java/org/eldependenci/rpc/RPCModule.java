package org.eldependenci.rpc;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.eldependenci.rpc.ELDRPCInstallation;
import org.eldependenci.rpc.implement.VersionGetter;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.eldependenci.rpc.remote.RemoteServiceProvider;
import org.eldependenci.rpc.retrofit.RetrofitServiceProvider;

import java.util.Map;
import java.util.Set;

public class RPCModule extends AbstractModule {

    private final ELDRPCInstallation eldrpcInstallation;
    private final VersionGetter getter;

    public RPCModule(ELDRPCInstallation eldrpcInstallation, VersionGetter getter) {
        this.eldrpcInstallation = eldrpcInstallation;
        this.getter = getter;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<Set<Class<?>>>() {
        }).annotatedWith(Names.named("eldrpc.serves")).toInstance(eldrpcInstallation.getServes());

        bind(new TypeLiteral<Map<String, RPCProtocol>>(){}).annotatedWith(Names.named("eldrpc.protocols")).toInstance(eldrpcInstallation.getProtocolMap());
        bind(VersionGetter.class).toInstance(getter);
        eldrpcInstallation.getRetrofits().forEach(retrofit -> bind(retrofit).toProvider(new RetrofitServiceProvider<>(retrofit)));
        eldrpcInstallation.getRemotes().forEach(rpc -> bind(rpc).toProvider(new RemoteServiceProvider<>(rpc)));

    }

}
