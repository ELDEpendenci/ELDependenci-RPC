package org.eldependenci.rpc.context;

import javax.annotation.Nullable;

/**
 * RPC 傳入內容協定
 * @param id 追蹤 ID
 * @param method 方法名稱
 * @param service 服務名稱
 * @param parameters 參數
 * @param token 認證 Token (由 {@link RPCInfo} 傳入)
 */
public record RPCPayload(long id, String method, String service, Object[] parameters, @Nullable String token) {
    public RPCPayload copyWithDiffToken(@Nullable String token) {
        return new RPCPayload(id, method, service, parameters, token);
    }

}
