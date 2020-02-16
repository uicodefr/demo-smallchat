package com.uicode.smallchat.smallchatserver.websocket.impl;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.service.ChannelService;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.service.UserService;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketServer;

import io.vertx.core.Promise;
import io.vertx.ext.auth.User;

public class WebSocketMediatorImpl implements WebSocketMediator {

    private final WebSocketServer webSocketServer;

    private final UserService userService;
    private final ChatStateService chatStateService;
    private final ChannelService channelService;

    @Inject
    public WebSocketMediatorImpl(
        WebSocketServer webSocketServer,
        UserService userService,
        ChatStateService chatStateService,
        ChannelService channelService
    ) {
        this.webSocketServer = webSocketServer;
        this.userService = userService;
        this.chatStateService = chatStateService;
        this.channelService = channelService;
    }

    @Override
    public Promise<User> authenticateUser(String jwtToken) {
        return userService.authenticate(jwtToken);
    }

    @Override
    public void connectUserForSubscription(String userId, String channelId, boolean connection) {
        webSocketServer.connectUserForSubscription(userId, channelId, connection);
    }

    @Override
    public void receiveUserConnection(String userId, boolean connection) {
        chatStateService.receiveUserConnection(userId, connection);
    }

    @Override
    public <T> void send(String channelId, T message) {
        webSocketServer.send(channelId, message);
    }

    @Override
    public void receiveSendMessage(String userId, String channelId, String message) {
        channelService.sendMessage(userId, channelId, message);
    }

}
