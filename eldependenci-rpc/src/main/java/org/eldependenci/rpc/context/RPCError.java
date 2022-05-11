package org.eldependenci.rpc.context;

/**
 * 錯誤協定
 * @param code 錯誤代碼
 * @param message 錯誤訊息
 * @param errors 錯誤追蹤
 */
public record RPCError(int code, String message, String[] errors) {
}
