package org.eldependenci.rpc.implement;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.*;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;
import org.eldependenci.rpc.context.*;
import org.eldependenci.rpc.protocol.RPCRequester;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OkWebSocketRequester implements RPCRequester {


    @Inject
    @Named("eld-json")
    private ObjectMapper mapper;

    private final DebugLogger logger;

    private final Map<Long, Consumer<Object>> callbackMap = new ConcurrentHashMap<>();

    @Inject
    public OkWebSocketRequester(LoggingService loggingService) {
        this.logger = loggingService.getLogger(OkWebSocketRequester.class);
    }

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.SECONDS)
            .connectTimeout(2, TimeUnit.SECONDS)
            .build();
    ;
    private @Nullable String serviceName;

    private List<RPCInfo.FallbackHost> hosts;
    private String token;
    private WebSocket webSocket;

    private RPCInfo info;

    @Override
    public CompletableFuture<Void> initialize(RPCInfo client) {
        this.info = client;
        this.serviceName = client.serviceName();
        this.hosts = client.fallbackHosts();
        hosts.add(new RPCInfo.FallbackHost(client.host(), client.useTLS(), client.authToken()));
        httpClient.connectionPool().evictAll();
        return launchWebSockets(hosts.iterator(), client.locate());
    }


    private CompletableFuture<Void> launchWebSockets(Iterator<RPCInfo.FallbackHost> hosts, String locate) {

        if (!hosts.hasNext())
            return CompletableFuture.failedFuture(new IllegalStateException("Failed to connect to any hosts for service: " + locate));
        var host = hosts.next();

        logger.debug("Connecting to host: {0}", host.host());

        var future = new CompletableFuture<Void>();
        launchWebSocket(host, serviceName).whenComplete((ws, ex) -> {
            if (ex != null) {
                logger.warn("Failed to connect to host: {0} for service, using others if any", host.host(), locate);
                launchWebSockets(hosts, locate).whenComplete((v, ex2) -> {
                    if (ex2 != null) {
                        logger.debug("throwing error: {0}", ex2.getMessage());
                        future.completeExceptionally(ex2);
                    } else {
                        logger.debug("finalizing completion");
                        future.complete(null);
                    }
                });
            } else {
                logger.debug("Connected to host: {0}", host.host());
                this.webSocket = ws;
                this.token = host.authToken();
                future.complete(null);
            }
        });

        return future;
    }


    private CompletableFuture<WebSocket> launchWebSocket(RPCInfo.FallbackHost host, String locate) {
        var future = new CompletableFuture<WebSocket>();
        this.httpClient.newWebSocket(
                new Request.Builder()
                        .url(buildURL(host))
                        .build(),
                new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        logger.debug("Successfully connected to {0} for service {1}", host.host(), locate);
                        future.complete(webSocket);
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable ex, @org.jetbrains.annotations.Nullable Response response) {
                        logger.info("Failed to connect to {0} for service {1}: {2}", host.host(), locate, ex.getMessage());
                        if (ex instanceof IOException && !(ex instanceof ConnectException)) {
                            logger.debug(ex);
                        } else if (!(ex instanceof IOException)) {
                            logger.warn(ex);
                        }
                        future.completeExceptionally(ex);
                        webSocket.close(1000, null);
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        logger.warn("WebSocket closed unexpectedly: {0}, reconnecting...", reason);
                        ((RealWebSocket) webSocket).connect(httpClient);
                    }


                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        try {
                            var response = mapper.readValue(text, RPCResponse.class);
                            handleResponse(response);
                        } catch (IOException e) {
                            logger.warn(e, "failed to parse response");
                        }
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        try {
                            var response = mapper.readValue(bytes.toByteArray(), RPCResponse.class);
                            handleResponse(response);
                        } catch (IOException e) {
                            logger.warn(e, "failed to parse response");
                        }
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<Object> offerRequest(RPCPayload payload)  {
        var previousFuture = webSocket == null ? launchWebSockets(hosts.iterator(), info.locate()).thenApply(v -> null) : CompletableFuture.completedFuture(null);
        return previousFuture.thenCompose(v -> {
            var future = new CompletableFuture<Object>();

            try {
                var b = mapper.writeValueAsBytes(payload.copyWithDiffToken(token));
                this.callbackMap.put(payload.id(), o -> {
                    if (o instanceof RPCError err) {
                        logger.warn("Error while handling websocket response: {0}", err.message());
                        future.completeExceptionally(new Exception(err.message()));
                    } else {
                        future.complete(o);
                    }
                });
                boolean success = webSocket.send(ByteString.of(b));
                logger.debug("websocket send success: {0}", success);
            } catch (IOException e) {
                logger.warn("failed to send payload: {0}", e.getMessage());
                future.completeExceptionally(e);
            }

            return future;
        });
    }

    private void handleResponse(RPCResponse<?> response) {
        if (response.success()) {
            var rpcResult = mapper.convertValue(response.result(), RPCResult.class);
            Optional.ofNullable(this.callbackMap.get(response.id()))
                    .ifPresentOrElse(
                            future -> future.accept(rpcResult.result()),
                            () -> logger.warn("No callback found for response {0}", response.id())
                    );
        } else {
            var err = mapper.convertValue(response.result(), RPCError.class);
            Optional.ofNullable(this.callbackMap.get(response.id()))
                    .ifPresentOrElse(
                            future -> future.accept(err),
                            () -> logger.warn("No callback found for response {0}", response.id())
                    );
        }
    }


    private String buildURL(RPCInfo.FallbackHost client) {
        return String.format("%s://%s/ws", client.useTLS() ? "wss" : "ws", client.host());
    }
}
