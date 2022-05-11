package org.eldependenci.rpc.context;

/**
 * RPC錯誤類
 */
public class RPCException extends Exception {


    private final long id;
    private final Exception real;

    /**
     * 建構子
     * @param id 追蹤 ID
     * @param e 真正的錯誤
     */
    public RPCException(long id, Exception e) {
        this.real = e;
        this.id = id;
    }

    /**
     * 返回 真正的錯誤
     * @return 真正的錯誤
     */
    public Exception getReal() {
        return real;
    }

    /**
     * 返回 追蹤 ID
     * @return 追蹤 ID
     */
    public long getId() {
        return id;
    }
}
