package org.eldependenci.rpc.protocol;

import org.eldependenci.rpc.context.RPCInfo;
import org.eldependenci.rpc.context.RPCPayload;

import java.util.concurrent.CompletableFuture;

/**
 * 用於定義 新協定 (Protocol) 的 RPC 請求接口
 */
public interface RPCRequester {

    /**
     * 初始化 請求接口 (基於 Remote 服務)
     *
     * @param client RPC 資訊
     * @return 異步初始化
     */
    CompletableFuture<Void> initialize(RPCInfo client);

    /**
     * 執行 RPC 請求
     * @param payload RPC 請求內容協定
     * @return RPC 回應
     */
    CompletableFuture<Object> offerRequest(RPCPayload payload);

}
