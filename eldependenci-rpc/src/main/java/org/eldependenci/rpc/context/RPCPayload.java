package org.eldependenci.rpc.context;

public record RPCPayload(long id, String method, String service, Object[] parameters) {
}
