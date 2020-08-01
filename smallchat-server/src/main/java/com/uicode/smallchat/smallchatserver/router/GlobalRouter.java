package com.uicode.smallchat.smallchatserver.router;

import java.util.Objects;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.service.GlobalService;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class GlobalRouter {

    private final Vertx vertx;
    private final GlobalService globalService;

    @Inject
    public GlobalRouter(Vertx vertx, GlobalService globalService) {
        this.vertx = vertx;
        this.globalService = globalService;
    }

    public void mountSubRouter(Router mainRouter) {
        Router globalRouter = Router.router(vertx);
        globalRouter.get("/info").handler(this::getInfo);
        globalRouter.get("/status").handler(this::getStatus);
        globalRouter.get("/likes.count").handler(this::countLikes);
        globalRouter.post("/likes").handler(this::addLike);
        mainRouter.mountSubRouter("/global", globalRouter);
    }

    private void getInfo(RoutingContext requestHandler) {
        JsonObject appInfo = globalService.getAppInfo();
        requestHandler.response().end(Objects.toString(appInfo));
    }

    private void getStatus(RoutingContext requestHandler) {
        MainRouter.mapResponse(globalService.getStatus(), requestHandler);
    }

    private void countLikes(RoutingContext requestHandler) {
        MainRouter.mapResponse(globalService.countLikes(), requestHandler);
    }

    private void addLike(RoutingContext requestHandler) {
        String remoteAddr = requestHandler.request().remoteAddress().host();
        MainRouter.mapResponse(globalService.addLike(remoteAddr), requestHandler);
    }

}
