package com.uicode.smallchat.smallchatserver.websocket.impl;

import com.google.inject.Inject;
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

    @Inject
    public WebSocketMediatorImpl(
        WebSocketServer webSocketServer,
        UserService userService,
        ChatStateService chatStateService
    ) {
        this.webSocketServer = webSocketServer;
        this.userService = userService;
        this.chatStateService = chatStateService;
    }

    @Override
    public <T> void send(String channel, T message) {
        webSocketServer.send(channel, message);
    }

    @Override
    public Promise<User> authenticateUser(String jwtToken) {
        return userService.authenticate(jwtToken);
    }

    @Override
    public void receiveUserConnection(String userId, boolean connection) {
        chatStateService.receiveUserConnection(userId, connection);
    }

}
