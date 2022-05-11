package org.eldependenci.rpc.context;

public record RPCResponse<T>(long id, boolean success, T result) {
}
