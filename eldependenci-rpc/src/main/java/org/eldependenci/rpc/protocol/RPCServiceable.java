package org.eldependenci.rpc.protocol;

/**
 * 用於 定義 新協定 (Protocol) 的 RPC 回應接口
 */
public interface RPCServiceable {

    /**
     * 啟動 Serve 服務
     * @param handler 服務處理器
     */
    void StartService(ServiceHandler handler);

    /**
     * 停止 Serve 服務
     */
    void StopService();

}
