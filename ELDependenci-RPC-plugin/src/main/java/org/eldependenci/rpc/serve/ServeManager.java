package org.eldependenci.rpc.serve;

import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.ericlam.mc.eld.services.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import org.eldependenci.rpc.ELDependenciRPC;
import org.eldependenci.rpc.RPCConfig;
import org.eldependenci.rpc.exception.ServiceException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ServeManager {

    private Javalin app;
    private final DebugLogger logger;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ServiceManager serviceManager;

    @Inject
    private RPCConfig config;

    @Inject
    private ELDependenciRPC plugin;


    @Inject
    public ServeManager(LoggingService loggingService) {
        this.logger = loggingService.getLogger(ServeManager.class);
    }

    public void startServe() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ELDependenciRPC.class.getClassLoader());

        // use our own object mapper so that to use bukkit object serializer/deserializer
        this.app = Javalin.create(c -> {
            c.jsonMapper(new JavalinJackson(objectMapper));
            c.asyncRequestTimeout = config.asyncTimeout;
        });

        var route = app.start(config.servePort);

        route.get("/", ctx -> {
            ctx.json(Map.of(
                    "status", "ok",
                    "version", plugin.getDescription().getVersion()
            ));
        });

        route.post("/{service}/{method}", ctx -> {

            logger.debug("looking for method {0} in service {1}", ctx.pathParam("method"), ctx.pathParam("service"));

            var serviceName = ctx.pathParam("service");
            var service = serviceManager.getService(serviceName);
            var methodName = ctx.pathParam("method");
            var method = serviceManager.getServiceMethod(service, methodName);

            RPCPayload payload;

            if (method.getParameterCount() == 0) {

                payload = new RPCPayload();
                payload.parameters = new Object[0];


            } else {
                payload = ctx.bodyAsClass(RPCPayload.class);
            }


            var async = method.isAnnotationPresent(DoAsync.class);

            logger.debug("method {0} in service {1} running async: {2}", methodName, serviceName, async);
            logger.debug("received parameters: {0}", Arrays.toString(payload.parameters));

            if (async) {
                ctx.future(CompletableFuture.supplyAsync(() -> {
                    try {
                        var returned = serviceManager.invokeServiceMethod(service, method, payload.parameters);
                        return serviceManager.validateReturned(returned, method.getReturnType());
                    } catch (Exception e) {
                        return e;
                    }
                }), (result) -> handleFutureResult(result, ctx, serviceName, methodName));

            } else {

                var returned = serviceManager.invokeServiceMethod(service, method, payload.parameters);

                if (returned instanceof ScheduleService.BukkitPromise<?> promise) {

                    logger.debug("method {0} in service {1} is returning bukkit promise", methodName, serviceName);
                    var future = new CompletableFuture<>();
                    promise.thenRunAsync(re -> {
                        var result = objectMapper.convertValue(re, Object.class);
                        logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);
                        future.complete(result);

                    }).joinWithCatch(future::completeExceptionally);

                    ctx.future(future,  (result) -> handleFutureResult(result, ctx, serviceName, methodName));

                }else{

                    var result = serviceManager.validateReturned(returned, method.getReturnType());
                    var res = new RPCResponse();
                    res.service = serviceName;
                    res.method = methodName;
                    res.result = result;
                    logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);
                    ctx.status(200).json(res);
                }

            }
        });

        route.exception(Exception.class, this::handleException);

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void handleFutureResult(Object result, Context ctx, String serviceName, String methodName) {
        if (result instanceof Exception ex) {
            this.handleException(ex, ctx);
        } else {
            var res = new RPCResponse();
            res.service = serviceName;
            res.method = methodName;
            res.result = result;

            logger.debug("method {0} in service {1} returning result: {2}", methodName, serviceName, result);

            ctx.status(200).json(res);
        }
    }

    private void handleException(Exception e, Context ctx) {

        if (e instanceof ServiceException) {
            logger.debug("Resolved Exception: {0} => {1}", e.getClass().getSimpleName(), e.getMessage());
        } else {
            logger.warn("Resolved Exception: {0} => {1} ", e.getClass().getSimpleName(), e.getMessage());
            logger.debug(e);
        }

        var code = e instanceof ServiceException ? 400 : 500;
        ctx.status(code);
        var err = new ServiceError();
        err.code = code;
        err.message = e.getMessage();
        var debug = ctx.queryParam("debug") != null;
        err.errors = debug ? Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new) : new String[0];
        ctx.status(code).json(err);
    }


    public void stop() {
        if (app != null) {
            app.close();
        }
    }

    public static class RPCPayload {
        public Object[] parameters;

    }

    public static class RPCResponse {
        public String service;
        public String method;
        public Object result;
    }


    public static class ServiceError {

        public int code;
        public String message;
        public String[] errors;

    }
}
