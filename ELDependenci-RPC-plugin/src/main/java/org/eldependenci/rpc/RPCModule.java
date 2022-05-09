package org.eldependenci.rpc;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.eldependenci.rpc.retrofit.RetrofitServiceProvider;

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
        eldrpcInstallation.getRetrofits().forEach(rpc -> bind(rpc).toProvider(new RetrofitServiceProvider<>(rpc)));
    }

}
