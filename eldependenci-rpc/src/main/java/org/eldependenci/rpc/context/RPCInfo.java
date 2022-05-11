package org.eldependenci.rpc.context;

import javax.annotation.Nullable;
import java.util.List;

/**
 * RPC 遠端資訊
 * @param locate Service Class 位置
 * @param host 遠端位址
 * @param protocol 遠端通訊協定
 * @param serviceName 遠端服務名稱, 如無則使用 Service Class 作為名稱
 * @param useTLS 是否使用 TLS
 * @param fallbackHosts 後備位址
 */
public record RPCInfo(String locate, String host, String protocol, @Nullable String serviceName, boolean useTLS, List<FallbackHost> fallbackHosts) {

    public record FallbackHost(String host, boolean useTLS) {
    }

}


