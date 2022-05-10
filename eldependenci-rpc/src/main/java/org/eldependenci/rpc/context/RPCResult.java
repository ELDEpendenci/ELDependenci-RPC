package org.eldependenci.rpc.context;

public record RPCResult(String method, String service, Object result) {
}
