package org.eldependenci.rpc.retrofit;

import com.google.inject.Inject;
import com.google.inject.Provider;

public final class RetrofitServiceProvider<T> implements Provider<T> {

    @Inject
    private RetrofitManager retrofitManager;

    private final Class<T> clazz;

    public RetrofitServiceProvider(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T get() {
        return retrofitManager.getRetrofitService(clazz);
    }

}
