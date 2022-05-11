package org.eldependenci.rpc.context;

/**
 * RPC 回傳內容協定
 * @param id 追蹤 ID
 * @param success 回傳是否成功
 * @param result 回傳結果, {@link RPCResult} 或 {@link RPCError}
 * @param <T> 回傳結果中的任一類型
 */
public record RPCResponse<T>(long id, boolean success, T result) {
}
