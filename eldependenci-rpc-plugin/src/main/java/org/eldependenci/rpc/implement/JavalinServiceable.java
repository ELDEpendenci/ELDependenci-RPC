package org.eldependenci.rpc.implement;

import com.ericlam.mc.eld.ELDPlugin;
import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.websocket.WsBinaryMessageContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.eldependenci.rpc.JsonMapperFactory;
import org.eldependenci.rpc.config.RPCConfig;
import org.eldependenci.rpc.context.*;
import org.eldependenci.rpc.protocol.RPCServiceable;
import org.eldependenci.rpc.protocol.ServiceHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public final class JavalinServiceable implements RPCServiceable {

    private boolean started = false;

    private Javalin app;
    private final DebugLogger logger;
    private final ObjectMapper objectMapper;

    @Inject
    private RPCConfig config;

    @Inject
    private VersionGetter versionGetter;

    @Inject
    public JavalinServiceable(LoggingService loggingService, JsonMapperFactory factory) {
        this.logger = loggingService.getLogger(JavalinServiceable.class);
        this.objectMapper = factory.jsonMapper();
    }


    @Override
    public synchronized void StartService(ServiceHandler handler) {

        if (started) return;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ELDPlugin.class.getClassLoader());

        // use our own object mapper so that to use bukkit object serializer/deserializer
        this.app = Javalin.create(c -> {
            c.jsonMapper(new JavalinJackson(objectMapper));
            c.asyncRequestTimeout = config.asyncTimeout;
        });

        var route = app.start(config.servePort);
        started = true;

        route.get("/", ctx -> {
            ctx.json(Map.of(
                    "status", "ok",
                    "version", versionGetter.getVersion()
            ));
        });


        route.ws("/ws", ws -> {

            ws.onConnect(ctx -> logger.info("RPC Client connected: {0}", ctx.getSessionId()));
            ws.onClose(ctx -> logger.info("RPC Client disconnected: {0}", ctx.getSessionId()));
            ws.onError(ctx -> {
                if (ctx.error() != null) {
                    logger.warn("RPC Client error: {0} => {1}", ctx.getSessionId(), ctx.error().getMessage());
                    logger.debug(ctx.error());
                }
            });

            ws.onMessage(ctx -> {
                handleWS(ctx, handler).whenComplete((v, e) -> {
                    if (e != null) {
                        logger.warn(e, "Error while handing message from {0}: {1}", ctx.getSessionId(), e.getMessage());
                    }
                });

            });

            ws.onBinaryMessage(ctx -> {
                handleWS(ctx, handler).whenComplete((v, e) -> {
                    if (e != null) {
                        logger.warn(e, "Error while handing message from {0}: {1}", ctx.getSessionId(), e.getMessage());
                    }
                });
            });

        });

        route.post("/rpc", ctx -> {


            RPCPayload rpcPayload = ctx.bodyAsClass(RPCPayload.class);

            var methodName = rpcPayload.method();
            var serviceName = rpcPayload.service();

            logger.debug("looking for method {0} in service {1}", methodName, serviceName);

            try {

                var future = handler.handlePayload(rpcPayload, ctx.queryParam("debug") != null);
                ctx.future(future.thenApply(result -> new RPCResponse<>(rpcPayload.id(), result instanceof RPCResult, result)));

            } catch (Exception e) {
                throw new RPCException(rpcPayload.id(), e);
            }
        });

        route.exception(Exception.class, (e, ctx) -> {
            e = toNestedException(e);
            var err = handler.toRPCError(e, ctx.queryParam("debug") != null);
            var rpcResponse = new RPCResponse<>(-1, false, err);
            ctx.status(err.code()).json(rpcResponse);
        });

        route.wsException(Exception.class, (e, ctx) -> {
            e = toNestedException(e);
            var err = handler.toRPCError(e, ctx.queryParam("debug") != null);
            var rpcResponse = new RPCResponse<>(-1, false, err);
            ctx.send(rpcResponse);
        });

        route.wsException(RPCException.class, (e, ctx) -> {
            var err = handler.toRPCError(e.getReal(), ctx.queryParam("debug") != null);
            var rpcResponse = new RPCResponse<>(e.getId(), false, err);
            ctx.send(rpcResponse);
        });

        route.exception(RPCException.class, (e, ctx) -> {
            var err = handler.toRPCError(e.getReal(), ctx.queryParam("debug") != null);
            var rpcResponse = new RPCResponse<>(e.getId(), false, err);
            ctx.status(err.code()).json(rpcResponse);
        });

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private Exception toNestedException(Exception e) {
        while (e.getCause() != null && e.getCause() instanceof Exception cause) {
            e = cause;
        }
        return e;
    }


    private CompletableFuture<Void> handleWS(WsContext ctx, ServiceHandler handler) throws Exception {

        RPCPayload rpcPayload;

        if (ctx instanceof WsBinaryMessageContext bx) {
            rpcPayload = objectMapper.readValue(bx.data(), RPCPayload.class);
        } else if (ctx instanceof WsMessageContext mx) {
            rpcPayload = mx.messageAsClass(RPCPayload.class);
        } else {
            throw new UnsupportedOperationException("unknown context: " + ctx);
        }

        logger.debug("looking for method {0} in service {1}", rpcPayload.method(), rpcPayload.service());

        try {

            var future = handler.handlePayload(rpcPayload, false);

            return future.thenAcceptAsync(result -> {

                try {

                    var response = new RPCResponse<>(rpcPayload.id(), result instanceof RPCResult, result);

                    if (ctx instanceof WsBinaryMessageContext bx) {
                        var b = objectMapper.writeValueAsBytes(response);
                        bx.send(ByteBuffer.wrap(b));
                    } else {
                        WsMessageContext mx = (WsMessageContext) ctx;
                        var s = objectMapper.writeValueAsString(response);
                        mx.send(s);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CompletionException(e);
                }
            });

        } catch (Exception e) {

            throw new RPCException(rpcPayload.id(), e);
        }

    }


    @Override
    public synchronized void StopService() {
        if (app != null) {
            app.close();
        }
    }
}
