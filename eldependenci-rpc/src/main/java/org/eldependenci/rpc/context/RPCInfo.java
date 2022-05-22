package org.eldependenci.rpc.context;

import org.eldependenci.rpc.annotation.Authorize;

import javax.annotation.Nullable;
import java.util.List;

/**
 * RPC 遠端資訊, 認證 token 將在 Serve 服務啟用 {@link Authorize} 的時候使用
 * @param locate Service Class 位置
 * @param host 遠端位址
 * @param protocol 遠端通訊協定
 * @param serviceName 遠端服務名稱, 如無則使用 Service Class 作為名稱
 * @param useTLS 是否使用 TLS
 * @param fallbackHosts 後備位址
 * @param authToken 認證 Token (如果該 Serve 類別啟用了授權請求，則必須指定)
 */
public record RPCInfo(
        String locate,
        String host, String protocol,
        @Nullable String serviceName,
        boolean useTLS,
        @Nullable String authToken,
        List<FallbackHost> fallbackHosts
) {

    /**
     * 後備位址
     * @param host 位址
     * @param useTLS 是否使用 TLS
     * @param authToken 認證 Token
     */
    public record FallbackHost(String host, boolean useTLS, @Nullable String authToken){
    }

}


