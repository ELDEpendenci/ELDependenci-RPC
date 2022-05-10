package org.eldependenci.rpc.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.*;
import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.context.RPCResponse;
import org.eldependenci.rpc.context.RPCResult;
import org.eldependenci.rpc.remote.RPCClient;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.eldependenci.rpc.remote.RemoteInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class OkHttpRequester implements RPCRequester {

    private final OkHttpClient client = new OkHttpClient();

    @Inject
    @Named("eld-json")
    private ObjectMapper mapper;

    private static final MediaType APPLICATION_JSON = MediaType.get("application/json; charset=utf-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpRequester.class);

    @Override
    public CompletableFuture<Object> offerRequest(RPCPayload payload, RPCClient client) throws Exception {
        RequestBody body = RequestBody.create(APPLICATION_JSON, mapper.writeValueAsBytes(payload));
        var url = buildUrl(payload, client);
        Request request = new Request.Builder()
                .url(buildUrl(payload, client))
                .post(body)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (Response response = this.client.newCall(request).execute()) {

                if (response.body() == null) {
                    LOGGER.warn("Response body is null, so returning null.");
                    return null;
                }

                var rpcResponse = mapper.readValue(response.body().bytes(), RPCResponse.class);

                if (rpcResponse.success()) {
                    var rpcResult = mapper.convertValue(rpcResponse.result(), RPCResult.class);
                    return rpcResult.result();
                } else {
                    var err = mapper.convertValue(rpcResponse.result(), RPCError.class);
                    LOGGER.warn("RPC Response returning error: {}", err.message());
                    throw new CompletionException(new Exception(err.message()));
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    private String buildUrl(RPCPayload payload, RPCClient client) {
        return String.format("%s://%s/%s/%s", client.useTLS() ? "https" : "http", client.host(), payload.service(), payload.method());
    }
}
