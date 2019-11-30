package com.uicode.smallchat.smallchatserver.router;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.InvalidDataException;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ChatRouter {

    private final Vertx vertx;
    private final ChatStateService chatStateService;

    @Inject
    public ChatRouter(Vertx vertx, ChatStateService chatStateService) {
        this.vertx = vertx;
        this.chatStateService = chatStateService;
    }

    public void mountSubRouter(Router mainRouter) {
        Router chatStateRouter = Router.router(vertx);
        chatStateRouter.get("/state").handler(this::getChatState);
        chatStateRouter.post("/channels").handler(this::createChannel);
        chatStateRouter.patch("/channels/:id").handler(this::updateChannel);
        chatStateRouter.delete("/channels/:id").handler(this::deleteChannel);
        mainRouter.mountSubRouter("/chat", chatStateRouter);
    }

    private void getChatState(RoutingContext requestHandler) {
        MainRouter.mapResponse(chatStateService.getChatState(), requestHandler);
    }

    private void createChannel(RoutingContext requestHandler) {
        Channel newChannel = requestHandler.getBodyAsJson().mapTo(Channel.class);
        if (StringUtils.isBlank(newChannel.getId()) || StringUtils.isBlank(newChannel.getName())) {
            requestHandler.fail(new InvalidDataException("Channel is invalid"));
            return;
        }

        MainRouter.mapResponse(chatStateService.createChannel(newChannel), requestHandler);
    }

    private void updateChannel(RoutingContext requestHandler) {
        String channelId = requestHandler.request().getParam("id");
        if (StringUtils.isBlank(channelId)) {
            requestHandler.fail(new InvalidDataException("channelId is invalid"));
            return;
        }
        Channel channel = requestHandler.getBodyAsJson().mapTo(Channel.class);
        MainRouter.mapResponse(chatStateService.updateChannel(channelId, channel), requestHandler);
    }

    private void deleteChannel(RoutingContext requestHandler) {
        String channelId = requestHandler.request().getParam("id");
        if (StringUtils.isBlank(channelId)) {
            requestHandler.fail(new InvalidDataException("channelId is invalid"));
            return;
        }
        MainRouter.mapResponse(chatStateService.deleteChannel(channelId), requestHandler);
    }

}
