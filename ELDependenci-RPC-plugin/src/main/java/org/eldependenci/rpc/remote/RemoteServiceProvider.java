package org.eldependenci.rpc.remote;

import com.google.inject.Inject;
import com.google.inject.Provider;

public final class RemoteServiceProvider<T> implements Provider<T> {

    @Inject
    private RemoteManager remoteManager;

    private final Class<T> service;

    public RemoteServiceProvider(Class<T> service) {
        this.service = service;
    }

    @Override
    public T get() {
        return remoteManager.getRemoteService(service);
    }
}
