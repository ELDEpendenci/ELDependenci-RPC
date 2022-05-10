package org.eldependenci.rpc.context;

public record RPCResponse<T>(boolean success, T result) {
}
