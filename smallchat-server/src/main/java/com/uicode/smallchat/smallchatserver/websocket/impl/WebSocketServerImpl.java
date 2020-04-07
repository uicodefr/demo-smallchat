package com.uicode.smallchat.smallchatserver.websocket.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.model.user.UserPayload;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;
import com.uicode.smallchat.smallchatserver.util.HttpStatus;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMsg;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketServer;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

public class WebSocketServerImpl implements WebSocketServer {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketServerImpl.class);

    private static final String WS_PATH = "/websocket";

    private final WebSocketMediator webSocketMediator;

    private Map<String, Pair<ServerWebSocket, String>> connections;

    private Map<String, List<Pair<ServerWebSocket, String>>> subscriptions;


    @Inject
    public WebSocketServerImpl(WebSocketMediator webSocketMediator) {
        this.webSocketMediator = webSocketMediator;
        this.connections = new HashMap<>();
        this.subscriptions = new HashMap<>();
    }

    @Override
    public void handleWebSocket(ServerWebSocket serverWebSocket) {
        if (!WS_PATH.equals(serverWebSocket.path())) {
            LOGGER.warn("WebSocket wrong path : {}", serverWebSocket.path());
            serverWebSocket.reject(HttpStatus.BAD_REQUEST.value());
            return;
        }

        authenticate(serverWebSocket.headers()).future().onComplete(userResult -> {
            if (userResult.failed()) {
                LOGGER.warn("WebSocket authentication failed", userResult.cause());
                serverWebSocket.reject(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            User user = userResult.result();
            String userId = user.principal().getString(UserPayload.USERNAME_FIELD);
            Pair<ServerWebSocket, String> currentConnection = Pair.of(serverWebSocket, userId);

            connections.put(userId, currentConnection);
            webSocketMediator.receiveUserConnection(userId, true);
            getSubscriptionList(WebSocketMsg.CHAT_STATE_SUBJECT).add(currentConnection);

            serverWebSocket.endHandler(endHandler -> {
                connections.remove(userId);
                webSocketMediator.receiveUserConnection(userId, false);
                for (String subscriptionId : subscriptions.keySet()) {
                    getSubscriptionList(subscriptionId).remove(currentConnection);
                    webSocketMediator.disconnectSubscription(userId, subscriptionId);
                }
            });

            serverWebSocket.textMessageHandler(textHandler -> {
                try {
                    JsonObject receiveWebSocketMsg = new JsonObject(textHandler);
                    String subject = receiveWebSocketMsg.getString("subject");

                    switch (subject) {

                    case WebSocketMsg.CHANNEL_MESSAGE_SUBJECT:
                        JsonObject data = receiveWebSocketMsg.getJsonObject("data");
                        SendChannelMessageMsg sendChannelMessage = data.mapTo(SendChannelMessageMsg.class);
                        webSocketMediator.receiveSendMessage(userId, sendChannelMessage.getChannelId(), sendChannelMessage.getMessage());
                        break;

                    case WebSocketMsg.PING_SUBJECT:
                        serverWebSocket.writeTextMessage(Json.encode(WebSocketMsg.of(WebSocketMsg.PONG_SUBJECT, "pong")));
                        break;

                    default:
                        LOGGER.error("Message unknown : {}", textHandler.trim());
                        break;
                    }

                } catch (Exception exception) {
                    LOGGER.error(String.format("Receive bad message : %s", textHandler.trim()), exception);
                }
            });

            serverWebSocket.accept();
        });
    }

    private Promise<User> authenticate(MultiMap headers) {
        Promise<User> result = Promise.promise();

        if (headers.get(HttpHeaders.COOKIE) == null) {
            result.fail("Cookie is absent in the headers");
            return result;
        }

        Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(headers.get(HttpHeaders.COOKIE));
        Optional<Cookie> jwtTokenCookie = nettyCookies.stream()
                .filter(cookie -> GeneralConst.JWTTOKEN_COOKIE.equals(cookie.name())).findFirst();

        if (!jwtTokenCookie.isPresent()) {
            result.fail("JwtToken cookie is absent");
            return result;
        }

        this.webSocketMediator.authenticateUser(jwtTokenCookie.get().value()).future()
            .onComplete(authenticateResult -> {
                if (authenticateResult.failed()) {
                    result.fail(authenticateResult.cause());
                } else {
                    result.complete(authenticateResult.result());
                }
            });

        return result;
    }

    private List<Pair<ServerWebSocket, String>> getSubscriptionList(String subscriptionId) {
        return this.subscriptions.computeIfAbsent(subscriptionId, key -> new ArrayList<>());
    }

    @Override
    public void connectUserForSubscription(String userId, String subscriptionId, boolean connection) {
        Pair<ServerWebSocket, String> userConnection = connections.get(userId);
        if (userConnection == null) {
            LOGGER.error("No userConnection for userId : {}", userId);
            return;
        }

        if (connection) {
            // Connect
            getSubscriptionList(subscriptionId).add(userConnection);
        } else {
            // Disconnect
            getSubscriptionList(subscriptionId).remove(userConnection);
        }
    }

    @Override
    public <T> void send(String subscriptionId, T message) {
        String subject = subscriptionId;
        if (subscriptionId.startsWith(GeneralConst.SUBSCRIPTION_CHANNEL_PREFIX)) {
            subject = WebSocketMsg.CHANNEL_MESSAGE_SUBJECT;
        }
        String finalSubject = subject;

        getSubscriptionList(subscriptionId).forEach(connection -> {
            ServerWebSocket serverWebSocket = connection.getLeft();
            if (!serverWebSocket.isClosed()) {
                serverWebSocket.writeTextMessage(Json.encode(WebSocketMsg.of(finalSubject, message)));
            }
        });
    }

}
