package org.eldependenci.rpc.context;

/**
 * RPC 標準回傳內容協定
 * @param method 被使用方法名稱
 * @param service 被使用服務名稱
 * @param result 回傳結果
 */
public record RPCResult(String method, String service, Object result) {
}
