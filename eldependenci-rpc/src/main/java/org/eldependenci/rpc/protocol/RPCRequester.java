package org.eldependenci.rpc.protocol;

import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.remote.RPCClient;

import java.util.concurrent.CompletableFuture;

public interface RPCRequester {

    CompletableFuture<Object> offerRequest(RPCPayload payload, RPCClient client) throws Exception;

}
