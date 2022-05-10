package org.eldependenci.rpc.context;

public record RPCPayload(String method, String service, Object[] parameters) {
}
