package com.uicode.smallchat.smallchatserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uicode.smallchat.smallchatserver.model.channel.ChannelFull;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.MessageCode;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.model.user.UserPayload;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMsg;
import com.uicode.smallchat.smallchatserver.websocket.impl.SendChannelMessageMsg;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava.core.Promise;

class IntegrationTest extends TestBase {

    private static final Logger LOGGER = LogManager.getLogger(IntegrationTest.class);

    private class TestData {
        public String jwtToken;
        public WebSocket webSocket;
        public List<String> wsMessages = new ArrayList<>();
    }

    @Test
    @DisplayName("Should send a message and received it")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void integrationTest(Vertx vertx, VertxTestContext testContext) throws Throwable {
        LOGGER.info("Test integrationTest started");
        WebClient webClient = WebClient.create(vertx);
        HttpClient httpClient = vertx.createHttpClient();

        // 1. Login
        login(webClient).future().compose(testData -> {
            // 1.b. Wait 2000ms after login for global initialization
            Promise<TestData> waitPromise = Promise.promise();
            vertx.setTimer(2000, timerResult -> waitPromise.complete(testData));
            return waitPromise.future();
        }).compose(testData ->
        // 2. WebSocket connection
        webSocketConnection(httpClient, testData).future()).compose(testData ->
        // 3. Create channel
        createChannel(webClient, testData).future()).compose(testData ->
        // 4. Connect to created channel
        connectToChannel(webClient, testData).future()).compose(testData ->
        // 5. Get chatState
        getChatState(webClient, testData).future()).compose(testData ->
        // 6. Send message
        sendMessage(webClient, testData).future()).compose(testData -> {
            // 6.b. Wait 500ms after sending message
            Promise<TestData> waitPromise = Promise.promise();
            vertx.setTimer(500, timerResult -> waitPromise.complete(testData));
            return waitPromise.future();
        }).compose(testData ->
        // 7. Get messages
        getMessages(webClient, testData).future())
            // End
            .onFailure(error -> {
                LOGGER.error("Test integrationTest failed", error);
                testContext.failNow(error);
            })
            .onComplete(data -> {
                LOGGER.info("Test integrationTest completed");
                testContext.completeNow();
            });
    }

    private Promise<TestData> login(WebClient webClient) {
        Promise<TestData> result = Promise.promise();
        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.add("username", "username");
        form.add("password", "password");

        webClient.post(8080, "localhost", "/login").as(BodyCodec.jsonObject()).sendForm(form, responseResult -> {
            Assertions.assertThat(responseResult.succeeded()).isTrue();
            Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);

            UserPayload userPayload = responseResult.result().body().mapTo(UserPayload.class);
            Assertions.assertThat(userPayload).isNotNull();
            Assertions.assertThat(userPayload.getUsername()).isEqualTo("username");
            Assertions.assertThat(userPayload.getRoles()).isNotEmpty();

            String setCookieHeader = responseResult.result().getHeader(HttpHeaders.SET_COOKIE.toString());
            Assertions.assertThat(setCookieHeader).isNotNull();
            Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(setCookieHeader);
            Optional<Cookie> jwtTokenCookie = nettyCookies.stream()
                .filter(cookie -> GeneralConst.JWTTOKEN_COOKIE.equals(cookie.name()))
                .findFirst();
            Assertions.assertThat(jwtTokenCookie).isPresent();
            String jwtTokenValue = jwtTokenCookie.get().value();
            Assertions.assertThat(jwtTokenValue).isNotEmpty();

            TestData testData = new TestData();
            testData.jwtToken = jwtTokenValue;
            result.complete(testData);
        });

        return result;
    }

    private Promise<TestData> webSocketConnection(HttpClient httpClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        WebSocketConnectOptions webSocketOptions = new WebSocketConnectOptions();
        webSocketOptions.setHost("localhost");
        webSocketOptions.setPort(8080);
        webSocketOptions.setURI("/websocket");
        webSocketOptions.addHeader(HttpHeaders.COOKIE.toString(),
                GeneralConst.JWTTOKEN_COOKIE + "=" + testData.jwtToken);

        httpClient.webSocket(webSocketOptions, webSocketResult -> {
            if (webSocketResult.failed()) {
                LOGGER.error("WebSocket connection failed", webSocketResult.cause());
            }
            Assertions.assertThat(webSocketResult.succeeded()).isTrue();

            webSocketResult.result().textMessageHandler(messageString -> {
                testData.wsMessages.add(messageString);
            });
            testData.webSocket = webSocketResult.result();
            result.complete(testData);
        });

        return result;
    }

    private Promise<TestData> createChannel(WebClient webClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        Channel newChannel = new Channel();
        newChannel.setId("testintegration");
        newChannel.setName("Test Integration Channel");
        newChannel.setDescription("Description");

        webClient.post(8080, "localhost", "/chat-state/channels")
            .putHeader(HttpHeaders.COOKIE.toString(), GeneralConst.JWTTOKEN_COOKIE + "=" + testData.jwtToken)
            .as(BodyCodec.jsonObject())
            .sendJson(newChannel, responseResult -> {
                Assertions.assertThat(responseResult.succeeded()).isTrue();
                Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);

                Channel createdChannel = responseResult.result().body().mapTo(Channel.class);
                Assertions.assertThat(createdChannel).isNotNull();
                Assertions.assertThat(createdChannel.getId()).isEqualTo(newChannel.getId());
                Assertions.assertThat(createdChannel.getName()).isEqualTo(newChannel.getName());
                Assertions.assertThat(createdChannel.getDescription()).isEqualTo(newChannel.getDescription());
                result.complete(testData);
            });

        return result;
    }

    private Promise<TestData> connectToChannel(WebClient webClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        webClient.post(8080, "localhost", "/channels/testintegration/connect")
            .putHeader(HttpHeaders.COOKIE.toString(), GeneralConst.JWTTOKEN_COOKIE + "=" + testData.jwtToken)
            .as(BodyCodec.jsonObject())
            .send(responseResult -> {
                Assertions.assertThat(responseResult.succeeded()).isTrue();
                Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);

                ChannelFull channelFull = responseResult.result().body().mapTo(ChannelFull.class);
                Assertions.assertThat(channelFull).isNotNull();
                Assertions.assertThat(channelFull.getId()).isEqualTo("testintegration");
                Assertions.assertThat(channelFull.getName()).isEqualTo("Test Integration Channel");
                Assertions.assertThat(channelFull.getDescription()).isEqualTo("Description");

                Assertions.assertThat(channelFull.getMessages()).hasSize(2);
                Assertions.assertThat(channelFull.getMessages().get(0).getCode()).isEqualTo(MessageCode.CREATED);
                Assertions.assertThat(channelFull.getMessages().get(0).getId()).isNotEmpty();
                Assertions.assertThat(channelFull.getMessages().get(0).getMessage()).isNotEmpty();
                Assertions.assertThat(channelFull.getMessages().get(1).getCode()).isEqualTo(MessageCode.CONNECT);
                Assertions.assertThat(channelFull.getMessages().get(1).getId()).isNotEmpty();
                Assertions.assertThat(channelFull.getMessages().get(1).getMessage()).isNotEmpty();

                result.complete(testData);
            });

        return result;
    }

    private Promise<TestData> getChatState(WebClient webClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        webClient.get(8080, "localhost", "/chat-state/")
            .putHeader(HttpHeaders.COOKIE.toString(), GeneralConst.JWTTOKEN_COOKIE + "=" + testData.jwtToken)
            .as(BodyCodec.jsonObject())
            .send(responseResult -> {
                Assertions.assertThat(responseResult.succeeded()).isTrue();
                Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);

                ChatState chatState = responseResult.result().body().mapTo(ChatState.class);
                Assertions.assertThat(chatState).isNotNull();
                Assertions.assertThat(chatState.getUsers()).hasSize(1);
                // The 2 automatic channels (test, welcome) and the created one
                Assertions.assertThat(chatState.getChannels()).hasSize(3);
                Assertions.assertThat(chatState.getUpdateDate()).isNotNull();

                result.complete(testData);
            });

        return result;
    }

    private Promise<TestData> sendMessage(WebClient webClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        SendChannelMessageMsg sendChannelMessage = new SendChannelMessageMsg();
        sendChannelMessage.setChannelId("testintegration");
        sendChannelMessage.setMessage("hello");
        WebSocketMsg<SendChannelMessageMsg> webSocketMsg = WebSocketMsg.of(WebSocketMsg.CHANNEL_MESSAGE_SUBJECT,
                sendChannelMessage);

        testData.webSocket.writeTextMessage(Json.encode(webSocketMsg), sendResult -> {
            if (sendResult.failed()) {
                LOGGER.error("WebSocket write failed", sendResult.cause());
            }
            Assertions.assertThat(sendResult.succeeded()).isTrue();
            result.complete(testData);
        });

        return result;
    }

    private Promise<TestData> getMessages(WebClient webClient, TestData testData) {
        Promise<TestData> result = Promise.promise();

        webClient.get(8080, "localhost", "/channels/testintegration")
            .putHeader(HttpHeaders.COOKIE.toString(), GeneralConst.JWTTOKEN_COOKIE + "=" + testData.jwtToken)
            .as(BodyCodec.jsonObject())
            .send(responseResult -> {
                Assertions.assertThat(responseResult.succeeded()).isTrue();
                Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);

                ChannelFull channelFull = responseResult.result().body().mapTo(ChannelFull.class);
                Assertions.assertThat(channelFull).isNotNull();
                Assertions.assertThat(channelFull.getId()).isEqualTo("testintegration");
                Assertions.assertThat(channelFull.getName()).isEqualTo("Test Integration Channel");
                Assertions.assertThat(channelFull.getDescription()).isEqualTo("Description");
                Assertions.assertThat(channelFull.getMessages()).hasSize(3);
                Assertions.assertThat(channelFull.getMessages().get(2).getCode()).isEqualTo(MessageCode.MSG);
                Assertions.assertThat(channelFull.getMessages().get(2).getId()).isNotEmpty();
                Assertions.assertThat(channelFull.getMessages().get(2).getUser()).isEqualTo("username");
                Assertions.assertThat(channelFull.getMessages().get(2).getMessage()).isEqualTo("hello");

                Assertions.assertThat(testData.webSocket.isClosed()).isFalse();
                Assertions.assertThat(testData.wsMessages).isNotEmpty();
                result.complete(testData);
            });

        return result;
    }

}
