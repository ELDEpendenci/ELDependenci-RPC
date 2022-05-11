package org.eldependenci.rpc.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.*;
import org.eldependenci.rpc.context.*;
import org.eldependenci.rpc.protocol.RPCRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class OkHttpRequester implements RPCRequester {

    private final OkHttpClient httpClient = new OkHttpClient();

    @Inject
    @Named("eld-json")
    private ObjectMapper mapper;

    private static final MediaType APPLICATION_JSON = MediaType.get("application/json; charset=utf-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpRequester.class);

    private List<RPCInfo.FallbackHost> hosts;
    private @Nullable String serviceName;

    @Override
    public void initialize(RPCInfo client) {
        this.serviceName = client.serviceName();
        this.hosts = client.fallbackHosts();
        // insert main host as first
        this.hosts.add(0, new RPCInfo.FallbackHost(client.host(), client.useTLS(), client.authToken()));
    }

    public Object invokeRequest(RPCPayload payload, RPCInfo.FallbackHost host) throws Exception {

        RequestBody body = RequestBody.create(APPLICATION_JSON, mapper.writeValueAsBytes(payload));
        Request request = new Request.Builder()
                .url(buildUrl(payload, host))
                .post(body)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {

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
                throw new Exception(err.message());
            }
        }
    }


    @Override
    public CompletableFuture<Object> offerRequest(RPCPayload payload) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();

        for (RPCInfo.FallbackHost host : this.hosts) {
            try {
                future.complete(invokeRequest(payload.copyWithDiffToken(host.authToken()), host));
                break;
            } catch (IOException e) {
                LOGGER.warn("service {} I/O error for host: {}, trying using others if any.", payload.service(), host.host());
            } catch (Exception e) {
                future.completeExceptionally(e);
                break;
            }
        }

        return future;
    }

    private String buildUrl(RPCPayload payload, RPCInfo.FallbackHost host) {
        return String.format("%s://%s/rpc", host.useTLS() ? "https" : "http", host.host(), Optional.ofNullable(serviceName).orElse(payload.service()), payload.method());
    }
}
