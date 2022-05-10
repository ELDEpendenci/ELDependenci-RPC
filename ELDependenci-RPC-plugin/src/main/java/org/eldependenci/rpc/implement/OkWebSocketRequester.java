package org.eldependenci.rpc.implement;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.*;
import okio.ByteString;
import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.context.RPCResponse;
import org.eldependenci.rpc.context.RPCResult;
import org.eldependenci.rpc.remote.RPCClient;
import org.eldependenci.rpc.protocol.RPCRequester;

import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OkWebSocketRequester implements RPCRequester{


    @Inject
    @Named("eld-json")
    private ObjectMapper mapper;


    private final DebugLogger logger;


    @Inject
    public OkWebSocketRequester(LoggingService loggingService){
        this.logger = loggingService.getLogger(OkWebSocketRequester.class);
    }

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public CompletableFuture<Object> offerRequest(RPCPayload payload, RPCClient client) throws Exception {
        var url = buildURL(client);
        var future = new CompletableFuture<>();
        this.client.newWebSocket(new Request.Builder().url(url).build(), new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                logger.debug("WebSocket opened for {0}", url);
                try {
                    var b = mapper.writeValueAsBytes(payload);
                    webSocket.send(ByteString.of(b));
                }catch (IOException e){
                    logger.warn(e, "failed to send payload");
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                logger.debug("WebSocket closed for {0}", url);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    var response = mapper.readValue(text, RPCResponse.class);
                    handleResponse(future, response);
                }catch (IOException e){
                    logger.warn(e, "failed to parse response");
                } finally {
                    webSocket.close(1000, "close");
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try {
                    var response = mapper.readValue(bytes.toByteArray(), RPCResponse.class);
                    handleResponse(future, response);
                }catch (IOException e){
                    logger.warn(e, "failed to parse response");
                } finally {
                    webSocket.close(1000, "close");
                }
            }
        });

        return future;
    }

    private void handleResponse(CompletableFuture<Object> future, RPCResponse<?> response){
        if (response.success()){
            var rpcResult = mapper.convertValue(response.result(), RPCResult.class);
            future.complete(rpcResult.result());
        } else {
            var err = mapper.convertValue(response.result(), RPCError.class);
            future.completeExceptionally(new Exception(err.message()));
        }
    }


    private String buildURL(RPCClient client) {
        return String.format("%s://%s/ws", client.useTLS() ? "wss" : "ws", client.host());
    }
}
