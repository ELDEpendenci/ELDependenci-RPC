package org.eldependenci.rpc;

import org.eldependenci.rpc.annotation.BaseURL;
import org.eldependenci.rpc.protocol.RPCProtocol;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.eldependenci.rpc.protocol.RPCServiceable;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ELDRPCInstallation implements RPCInstallation {

    private final Set<Class<?>> retrofits = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> remotes = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> serves = ConcurrentHashMap.newKeySet();

    private final Map<String, RPCProtocol> protocolMap = new ConcurrentHashMap<>();

    @Override
    public void retrofits(Class<?>... services) {
        for (Class<?> service : services) {
            try {
                this.validateRetrofit(service);
            } catch (IllegalStateException e) {
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
                this.validateRemote(service);
            } catch (IllegalStateException e) {
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

    @Override
    public void registerProtocol(String protocolName, Class<? extends RPCServiceable> serviceable, Class<? extends RPCRequester> requester) {
        this.protocolMap.put(protocolName, new RPCProtocol(protocolName, serviceable, requester));
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Class<T>> getRetrofits() {
        return retrofits.stream().map(l -> (Class<T>) l).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public <T> Set<Class<T>> getRemotes() {
        return remotes.stream().map(l -> (Class<T>) l).collect(Collectors.toSet());
    }

    public Set<Class<?>> getServes() {
        return serves;
    }

    public Map<String, RPCProtocol> getProtocolMap() {
        return protocolMap;
    }

    private void validateRetrofit(Class<?> service) throws IllegalStateException {
        if (!service.isAnnotationPresent(BaseURL.class)) {
            throw new IllegalStateException(String.format("Service %s 缺少 @BaseURL 標註。", service.getName()));
        }
        if (!service.isInterface()) {
            throw new IllegalStateException(String.format("Service %s 必須為 interface。", service.getName()));
        }
    }

    private void validateRemote(Class<?> service) throws IllegalStateException {
        if (!service.isInterface()) {
            throw new IllegalStateException(String.format("Service %s 必須為 interface。", service.getName()));
        }
    }
}
