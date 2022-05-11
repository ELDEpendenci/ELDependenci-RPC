package org.eldependenci.rpc.protocol;

public record RPCProtocol(String protocolName, Class<? extends RPCServiceable> serviceClass, Class<? extends RPCRequester> requester){
}
