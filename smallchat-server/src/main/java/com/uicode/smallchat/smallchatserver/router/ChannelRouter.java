package com.uicode.smallchat.smallchatserver.router;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.InvalidDataException;
import com.uicode.smallchat.smallchatserver.exception.UnauthorizedException;
import com.uicode.smallchat.smallchatserver.service.ChannelService;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ChannelRouter {

    private static final String INVALID_CHANNEL_ID_ERROR = "channelId is invalid";

    private final Vertx vertx;
    private final ChannelService channelService;

    @Inject
    public ChannelRouter(Vertx vertx, ChannelService channelService) {
        this.vertx = vertx;
        this.channelService = channelService;
    }

    public void mountSubRouter(Router mainRouter) {
        Router chatStateRouter = Router.router(vertx);
        chatStateRouter.get("/:id").handler(this::getChannel);
        chatStateRouter.post("/:id/connect").handler(this::connectToChannel);
        chatStateRouter.post("/:id/disconnect").handler(this::disconnectToChannel);
        mainRouter.mountSubRouter("/channels", chatStateRouter);
    }

    private void getChannel(RoutingContext requestHandler) {
        String channelId = requestHandler.request().getParam("id");
        if (StringUtils.isBlank(channelId)) {
            requestHandler.fail(new InvalidDataException(INVALID_CHANNEL_ID_ERROR));
            return;
        }
        MainRouter.mapResponse(channelService.getChannelFull(channelId), requestHandler);
    }

    private void connectToChannel(RoutingContext requestHandler) {
        String channelId = requestHandler.request().getParam("id");
        if (StringUtils.isBlank(channelId)) {
            requestHandler.fail(new InvalidDataException(INVALID_CHANNEL_ID_ERROR));
            return;
        }
        try {
            String userId = MainRouter.getUserId(requestHandler)
                .orElseThrow(() -> new UnauthorizedException("User need to sign in"));
            MainRouter.mapResponse(channelService.connect(userId, channelId), requestHandler);
        } catch (UnauthorizedException exception) {
            requestHandler.fail(exception);
        }
    }

    private void disconnectToChannel(RoutingContext requestHandler) {
        String channelId = requestHandler.request().getParam("id");
        if (StringUtils.isBlank(channelId)) {
            requestHandler.fail(new InvalidDataException(INVALID_CHANNEL_ID_ERROR));
            return;
        }
        try {
            String userId = MainRouter.getUserId(requestHandler)
                .orElseThrow(() -> new UnauthorizedException("User need to sign in"));
            MainRouter.mapResponse(channelService.disconnect(userId, channelId), requestHandler);
        } catch (UnauthorizedException exception) {
            requestHandler.fail(exception);
        }
    }

}
