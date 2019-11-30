package com.uicode.smallchat.smallchatserver.websocket;

import io.vertx.core.Promise;
import io.vertx.ext.auth.User;

public interface WebSocketMediator {

    <T> void send(String channel, T message);

    Promise<User> authenticateUser(String jwtToken);

    void receiveUserConnection(String userId, boolean connection);

}
