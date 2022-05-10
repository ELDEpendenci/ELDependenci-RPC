package org.eldependenci.rpc.implement;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.JsonMapperFactory;
import org.eldependenci.rpc.RPCConfig;
import org.eldependenci.rpc.context.RPCError;
import org.eldependenci.rpc.context.RPCPayload;
import org.eldependenci.rpc.context.RPCResponse;
import org.eldependenci.rpc.context.RPCResult;
import org.eldependenci.rpc.exception.ServiceException;
import org.eldependenci.rpc.protocol.RPCServiceable;
import org.eldependenci.rpc.protocol.ServiceHandler;
import org.eldependenci.rpc.serve.ThrowableSupplier;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class JavalinServiceable implements RPCServiceable {

    private boolean started = false;

    private Javalin app;
    private final DebugLogger logger;
    private final ObjectMapper objectMapper;

    @Inject
    private RPCConfig config;

    @Inject
    private ELDependenciRPC plugin;


    @Inject
    public JavalinServiceable(LoggingService loggingService, JsonMapperFactory factory) {
        this.logger = loggingService.getLogger(JavalinServiceable.class);
        this.objectMapper = factory.jsonMapper();
    }


    @Override
    public synchronized void StartService(ServiceHandler handler) {

        if (started) return;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ELDependenciRPC.class.getClassLoader());

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
                    "version", plugin.getDescription().getVersion()
            ));
        });


        route.ws("/ws", ws -> {

            ws.onConnect(ctx -> logger.info("RPC Client connected: {0}", ctx.getSessionId()));
            ws.onClose(ctx -> logger.info("RPC Client disconnected: {0}", ctx.getSessionId()));
            ws.onError(ctx -> {
                if (ctx.error() != null){
                    logger.warn("RPC Client error: {0} => {1}", ctx.getSessionId(), ctx.error().getMessage());
                    logger.debug(ctx.error());
                }
            });

            ws.onMessage(ctx -> {
                var response = handleWSMessage(() -> ctx.messageAsClass(WSPayload.class), handler);
                ctx.send(response);

            });
            ws.onBinaryMessage(ctx -> {
                var response = handleWSMessage(() -> objectMapper.readValue(ctx.data(), WSPayload.class), handler);
                ctx.send(response);
            });

        });

        route.post("/{service}/{method}", ctx -> {

            var serviceName = ctx.pathParam("service");
            var methodName = ctx.pathParam("method");

            logger.debug("looking for method {0} in service {1}", methodName, serviceName);

            HttpPayload httpPayload = ctx.bodyAsClass(HttpPayload.class);

            var rpcPayload = new RPCPayload(serviceName, methodName, httpPayload.parameters);
            var async = handler.shouldCallAsync(rpcPayload);

            logger.debug("method {0} in service {1} running async: {2}", methodName, serviceName, async);
            logger.debug("received parameters: {0}", Arrays.toString(httpPayload.parameters));

            if (async) {
                ctx.future(CompletableFuture.supplyAsync(() -> {
                    try {
                       var o = handler.invokes(rpcPayload);
                       return handler.finalizeType(o.result(), o.returnType());
                    } catch (Exception e) {
                        return e;
                    }
                }), (result) -> handleFutureResult(result, ctx, serviceName, methodName, handler));

            } else {

                var res = handler.invokes(rpcPayload);

                if (res.result() instanceof ScheduleService.BukkitPromise<?> promise) {

                    logger.debug("method {0} in service {1} is returning bukkit promise", methodName, serviceName);
                    var future = new CompletableFuture<>();
                    promise.thenRunAsync(re -> {
                        var result = objectMapper.convertValue(re, Object.class);
                        logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);
                        future.complete(result);

                    }).joinWithCatch(future::completeExceptionally);

                    ctx.future(future, (result) -> handleFutureResult(result, ctx, serviceName, methodName, handler));

                } else {

                    var result = handler.finalizeType(res.result(), res.returnType());
                    var rpcResponse = new RPCResponse<>(true, new RPCResult(methodName, serviceName, result));
                    logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);
                    ctx.status(200).json(rpcResponse);
                }

            }
        });

        route.exception(Exception.class, (e, ctx) -> {
            var err = handler.toRPCError(e, ctx.queryParam("debug") != null);
            var rpcResponse = new RPCResponse<>(false, err);
            ctx.status(err.code()).json(rpcResponse);
        });

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private RPCResponse<?> handleWSMessage(ThrowableSupplier<WSPayload> payloadGet, ServiceHandler handler) {

        Object result;

        try {

            var wsPayload = payloadGet.get();

            logger.debug("looking for method {0} in service {1}", wsPayload.method, wsPayload.service);

            var returned = handler.invokes(new RPCPayload(wsPayload.service, wsPayload.method, wsPayload.parameters));

            result = handler.finalizeType(returned.result(), returned.returnType());

        } catch (Exception e) {

            String[] errors = new String[0];

            if (!(e instanceof ServiceException)) {
                errors = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
                e.printStackTrace();
            }

            result = new RPCError(400, e.getMessage(), errors);
        }

        return new RPCResponse<>(!(result instanceof RPCError), result);
    }

    private void handleFutureResult(Object result, Context ctx, String serviceName, String methodName, ServiceHandler handler) {
        RPCResponse<?> content;

        if (result instanceof Exception ex) {
            var err  = handler.toRPCError(ex, ctx.queryParam("debug") != null);
            content = new RPCResponse<>(false, err);
        }else{
            content = new RPCResponse<>(true, new RPCResult(methodName, serviceName, result));
        }

        logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);
        ctx.status(200).json(content);
    }


    @Override
    public void StopService() {
        if (app != null) {
            app.close();
        }
    }


    public record HttpPayload(Object[] parameters) {
    }

    public record WSPayload(Object[] parameters, String method, String service){
    }
}
