package com.uicode.smallchat.smallchatserver.websocket;

import io.vertx.core.http.ServerWebSocket;

public interface WebSocketServer {

    void handleWebSocket(ServerWebSocket serverWebSocket);

    <T> void send(String channel, T message);

}
