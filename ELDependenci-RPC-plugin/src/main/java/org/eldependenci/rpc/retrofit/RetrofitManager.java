package org.eldependenci.rpc.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.eldependenci.rpc.remote.BaseURL;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RetrofitManager {

    private final Map<Class<?>, Object> retroServiceMap = new ConcurrentHashMap<>();

    @Inject
    private ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    public <T> T getRetrofitService(Class<T> clazz) {
        return Optional.ofNullable((T)retroServiceMap.get(clazz)).orElseGet(() -> {
            var service = generateRetrofitService(clazz);
            retroServiceMap.put(clazz, service);
            return service;
        });
    }

    private <T> T generateRetrofitService(Class<T> clazz) {
        var base = clazz.getAnnotation(BaseURL.class);
        Retrofit fit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .baseUrl(base.value())
                .build();
        return fit.create(clazz);
    }

}
