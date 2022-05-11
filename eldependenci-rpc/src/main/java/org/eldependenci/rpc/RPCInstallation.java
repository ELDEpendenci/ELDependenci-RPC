package org.eldependenci.rpc;

import org.eldependenci.rpc.protocol.RPCRequester;
import org.eldependenci.rpc.protocol.RPCServiceable;

/**
 * RPC 自定義安裝
 */
public interface RPCInstallation {

    /**
     * 註冊 Retrofit 服務
     * @param services Retrofit 服務
     */
    void retrofits(Class<?>... services);

    /**
     * 註冊 Remote 服務 (Consumer)
     * @param services Remote 服務
     */
    void remotes(Class<?>... services);

    /**
     * 註冊 Serve 服務 (Producer)
     * @param serves Serve 服務
     */
    void serves(Class<?>... serves);

    /**
     * 註冊新的 RPC 協定通訊
     * @param protocolName 協定通訊名稱
     * @param serviceable 協定通訊回應服務
     * @param requester 協定通訊請求服務
     */
    void registerProtocol(String protocolName, Class<? extends RPCServiceable> serviceable, Class<? extends RPCRequester> requester);


}
