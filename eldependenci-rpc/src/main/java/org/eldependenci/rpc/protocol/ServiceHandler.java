package org.eldependenci.rpc.protocol;

import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;

import java.lang.reflect.Type;

public interface ServiceHandler {

    Response invokes(RPCPayload payload) throws Exception;

    Object finalizeType(Object result, Type returnType) throws Exception;

    boolean shouldCallAsync(RPCPayload payload) throws Exception;

    RPCError toRPCError(Exception e, boolean debug);

    record Response(Object result, Type returnType){
    }

}
