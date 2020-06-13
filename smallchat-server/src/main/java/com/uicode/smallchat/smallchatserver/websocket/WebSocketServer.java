package com.uicode.smallchat.smallchatserver.websocket;

import com.uicode.smallchat.smallchatserver.exception.InvalidStateException;

import io.vertx.core.http.ServerWebSocket;

public interface WebSocketServer {

    void handleWebSocket(ServerWebSocket serverWebSocket);

    <T> void send(String channelId, T message);

    void connectUserForSubscription(String userId, String channelId, boolean connection) throws InvalidStateException;

}
