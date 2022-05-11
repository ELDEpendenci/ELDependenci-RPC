package org.eldependenci.rpc.protocol;

import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * 服務處理接口，用於傳入處理方法與參數
 */
public interface ServiceHandler {

    /**
     * 異步處理 payload
     * <p />
     * 此方式將包含使用 {@link #toFuture(RPCPayload)}, {@link #invokes(RPCPayload)} 和 {@link #finalizeType(Object, Type)},
     * 且自帶 BukkitPromise 轉換功能
     * @param rpcPayload 請求資料
     * @param debug 是否啟用除錯, 若是，則會填入 {@link RPCError#errors() } 參數
     * @exception Exception 處理異常
     * @return 異步處理
     */
    CompletableFuture<Object> handlePayload(RPCPayload rpcPayload, boolean debug) throws Exception;

    /**
     * 將 payload 的處理操作轉換成 異步
     * @param rpcPayload 請求資料
     * @param debug 是否啟用除錯, 若是，則會填入 {@link RPCError#errors() } 參數
     * @return 異步處理
     */
    CompletableFuture<Object> toFuture(RPCPayload rpcPayload, boolean debug);

    /**
     * 處理 RPC 請求
     * @param payload 請求資料
     * @return 處理結果
     * @throws Exception 處理異常
     */
    Response invokes(RPCPayload payload) throws Exception;

    /**
     * 轉換處理結果的型別
     * @param result 處理結果
     * @param returnType 處理方法的返回型別
     * @return 轉換後的處理結果
     * @throws Exception 轉換異常
     */
    Object finalizeType(Object result, Type returnType) throws Exception;

    /**
     * 查看該 RPC 請求是否允許異步
     * @param payload 請求資料
     * @return 是否允許異步
     * @throws Exception 查詢異常
     */
    boolean shouldCallAsync(RPCPayload payload) throws Exception;

    /**
     * 將錯誤轉換為 RPC 錯誤回傳協定
     * @param e 錯誤
     * @param debug 是否啟用除錯, 若是，則會填入 {@link RPCError#errors() } 參數
     * @return 錯誤回傳協定
     */
    RPCError toRPCError(Exception e, boolean debug);

    /**
     * 處理結果的型別
     * @param result 處理結果
     * @param returnType 處理方法的返回型別
     */
    record Response(Object result, Type returnType){
    }

}
