package org.eldependenci.rpc.context;

import org.eldependenci.rpc.annotation.Authorize;

/**
 * 認證 token 授權錯誤時，用於 {@link Authorize} 的 Serve 服務。
 */
public class RPCUnauthorizedException extends RPCException {

    /**
     * 認證 token 授權錯誤
     * @param id 追蹤 ID
     * @param e 原本錯誤
     */
    public RPCUnauthorizedException(long id, Exception e) {
        super(id, e);
    }

}
