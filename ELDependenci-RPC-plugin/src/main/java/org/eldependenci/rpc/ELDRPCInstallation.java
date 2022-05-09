package org.eldependenci.rpc;

import org.eldependenci.rpc.remote.BaseURL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ELDRPCInstallation implements RPCInstallation{

    private final Set<Class<?>> retrofits = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> remotes = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> serves = ConcurrentHashMap.newKeySet();

    @Override
    public void retrofits(Class<?>... services) {
        for (Class<?> service : services) {
            try {
                this.validateService(service);
            }catch (IllegalStateException e) {
                e.printStackTrace();
                continue;
            }
            this.retrofits.add(service);
        }
    }

    @Override
    public void remotes(Class<?>... services) {
        for (Class<?> service : services) {
            try {
                this.validateService(service);
            }catch (IllegalStateException e) {
                e.printStackTrace();
                continue;
            }
            this.remotes.add(service);
        }
    }

    @Override
    public void serves(Class<?>... serves) {
        this.serves.addAll(Arrays.asList(serves));
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Class<T>> getRetrofits() {
        return retrofits.stream().map(l -> (Class<T>)l).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Class<T>> getRemotes() {
        return remotes.stream().map(l -> (Class<T>)l).collect(Collectors.toSet());
    }

    public  Set<Class<?>> getServes() {
        return serves;
    }

    private void validateService(Class<?> service) throws IllegalStateException {
        if (!service.isAnnotationPresent(BaseURL.class)) {
            throw new IllegalStateException(String.format("Service %s 缺少 @BaseURL 標註。", service.getName()));
        }
        if (!service.isInterface()){
            throw new IllegalStateException(String.format("Service %s 必須為 interface。", service.getName()));
        }
    }
}
