package org.eldependenci.rpc.context;

/**
 * RPC錯誤類
 */
public class RPCException extends Exception {


    private final long id;
    private final Exception real;
    public RPCException(long id, Exception e) {
        this.real = e;
        this.id = id;
    }

    /**
     *
     * @return 真正的錯誤
     */
    public Exception getReal() {
        return real;
    }

    /**
     *
     * @return 追蹤 ID
     */
    public long getId() {
        return id;
    }
}
