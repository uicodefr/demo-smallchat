package com.uicode.smallchat.smallchatserver.websocket;

import io.vertx.core.Promise;
import io.vertx.ext.auth.User;

public interface WebSocketMediator {

    Promise<User> authenticateUser(String jwtToken);

    void connectUserForSubscription(String userId, String subscriptionId, boolean connection);

    void receiveUserConnection(String userId, boolean connection);

    <T> void send(String subscriptionId, T message);

    void receiveSendMessage(String userId, String channelId, String message);

}
