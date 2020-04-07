package com.uicode.smallchat.smallchatserver.router;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.HttpStatusReturn;
import com.uicode.smallchat.smallchatserver.model.global.ErrorResponse;
import com.uicode.smallchat.smallchatserver.model.user.UserPayload;
import com.uicode.smallchat.smallchatserver.util.HttpStatus;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainRouter {

    private static final String APPLICATION_JSON = "application/json";

    private static final Logger LOGGER = LogManager.getLogger(MainRouter.class);

    private final Router router;

    private final GlobalRouter globalRouter;
    private final UserRouter userRouter;
    private final ChatStateRouter chatStateRouter;
    private final ChannelRouter channelRouter;

    @Inject
    public MainRouter(
        Vertx vertx,
        GlobalRouter globalRouter,
        UserRouter userRouter,
        ChatStateRouter chatStateRouter,
        ChannelRouter channelRouter
    ) {
        router = Router.router(vertx);

        this.globalRouter = globalRouter;
        this.userRouter = userRouter;
        this.chatStateRouter = chatStateRouter;
        this.channelRouter = channelRouter;
    }

    public Router getRouter() {
        return router;
    }

    public void mountRouter() {
        router.route().handler(BodyHandler.create());

        router.route().consumes(APPLICATION_JSON);
        router.route().produces(APPLICATION_JSON);
        router.route().handler(requestHandler -> {
            requestHandler.response().putHeader("content-type", APPLICATION_JSON);
            requestHandler.next();
        });

        addErrorHandlers(router);

        userRouter.mountSubRouter(router);
        globalRouter.mountSubRouter(router);
        chatStateRouter.mountSubRouter(router);
        channelRouter.mountSubRouter(router);
        LOGGER.info("Routers mounted");
    }

    private void addErrorHandlers(Router router) {
        router.errorHandler(500, routingContext -> {
            Throwable error = routingContext.failure();
            ErrorResponse errorResponse = new ErrorResponse();

            HttpStatus status;
            if (error instanceof HttpStatusReturn) {
                status = ((HttpStatusReturn) error).getStatus();
                errorResponse.setMessage(error.getMessage());
                LOGGER.warn(error);
            } else {
                Pair<HttpStatus, String> statusAndMessage = handleTechException(error);
                status = statusAndMessage.getLeft();
                errorResponse.setMessage(statusAndMessage.getRight());
            }

            routingContext.response().setStatusCode(status.value());
            routingContext.response().setStatusMessage(status.getReasonPhrase());

            errorResponse.setStatusCode(status.value());
            routingContext.response().end(Json.encode(errorResponse));
        });

        router.errorHandler(404, routingContext -> {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatusCode(404);
            errorResponse.setMessage("Not Found");
            routingContext.response().setStatusCode(HttpStatus.NOT_FOUND.value());
            routingContext.response().end(Json.encode(errorResponse));
        });
    }

    private Pair<HttpStatus, String> handleTechException(Throwable error) {
        if (error instanceof DecodeException || error instanceof IllegalArgumentException) {
            return Pair.of(HttpStatus.BAD_REQUEST, "Invalid JSON");
        } else {
            // Unknown Exception as logged as error
            LOGGER.error("Unknown Error !", error);
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, error.getMessage());
        }
    }

    public static <T> void mapResponse(Promise<T> servicePromise, RoutingContext requestHandler) {
        servicePromise.future()
            .onFailure(requestHandler::fail)
            .onSuccess(serviceResult -> {
                if (serviceResult != null) {
                    requestHandler.response().end(Json.encode(serviceResult));
                } else {
                    requestHandler.response().end();
                }
            });
    }

    public static Optional<String> getUserId(RoutingContext requestHandler) {
        if (requestHandler.user() == null || requestHandler.user().principal() == null) {
            return Optional.empty();
        } else {
            String userId = requestHandler.user().principal().getString(UserPayload.USERNAME_FIELD);
            return Optional.of(userId);
        }
    }

}
