package org.eldependenci.rpc.context;

public class RPCUnauthorizedException extends RPCException {

    public RPCUnauthorizedException(long id, Exception e) {
        super(id, e);
    }

}
