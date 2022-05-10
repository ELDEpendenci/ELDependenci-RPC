package org.eldependenci.rpc.context;

public record RPCError(int code, String message, String[] errors) {
}
