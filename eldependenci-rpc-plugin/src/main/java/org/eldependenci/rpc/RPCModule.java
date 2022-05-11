package org.eldependenci.rpc;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.eldependenci.rpc.remote.RemoteServiceProvider;
import org.eldependenci.rpc.retrofit.RetrofitServiceProvider;

import java.util.Map;
import java.util.Set;

public class RPCModule extends AbstractModule {

    private final ELDRPCInstallation eldrpcInstallation;

    public RPCModule(ELDRPCInstallation eldrpcInstallation) {
        this.eldrpcInstallation = eldrpcInstallation;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<Set<Class<?>>>() {
        }).annotatedWith(Names.named("eldrpc.serves")).toInstance(eldrpcInstallation.getServes());

        bind(new TypeLiteral<Map<String, RPCProtocol>>(){}).annotatedWith(Names.named("eldrpc.protocols")).toInstance(eldrpcInstallation.getProtocolMap());

        eldrpcInstallation.getRetrofits().forEach(retrofit -> bind(retrofit).toProvider(new RetrofitServiceProvider<>(retrofit)));
        eldrpcInstallation.getRemotes().forEach(rpc -> bind(rpc).toProvider(new RemoteServiceProvider<>(rpc)));

    }

}
