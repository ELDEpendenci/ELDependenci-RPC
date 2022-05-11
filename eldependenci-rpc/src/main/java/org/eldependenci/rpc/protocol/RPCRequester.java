package org.eldependenci.rpc.protocol;

import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.context.RPCPayload;

import java.util.concurrent.CompletableFuture;

public interface RPCRequester {

    void initialize(RPCInfo client);

    CompletableFuture<Object> offerRequest(RPCPayload payload) throws Exception;

}
