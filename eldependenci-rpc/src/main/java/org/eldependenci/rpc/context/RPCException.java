package org.eldependenci.rpc.context;

public class RPCException extends Exception {

    private final long id;
    private final Exception real;
    public RPCException(long id, Exception e) {
        this.real = e;
        this.id = id;
    }

    public Exception getReal() {
        return real;
    }

    public long getId() {
        return id;
    }
}
