package org.eldependenci.rpc.context;

import javax.annotation.Nullable;

public record RPCPayload(long id, String method, String service, Object[] parameters, @Nullable String token) {
    public RPCPayload copyWithDiffToken(@Nullable String token) {
        return new RPCPayload(id, method, service, parameters, token);
    }

}
