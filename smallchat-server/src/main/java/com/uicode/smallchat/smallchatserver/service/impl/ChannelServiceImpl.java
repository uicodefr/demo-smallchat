package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.InvalidStateException;
import com.uicode.smallchat.smallchatserver.exception.runtime.NotFoundException;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelFull;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.MessageCode;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.ChannelNotice;
import com.uicode.smallchat.smallchatserver.service.ChannelService;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;

import io.vertx.core.Future;
import io.vertx.core.Promise;

public class ChannelServiceImpl implements ChannelService {

    private static final Logger LOGGER = LogManager.getLogger(ChannelServiceImpl.class);

    private static final String CONNECTION_MESSAGE = "%s has joined";
    private static final String DISCONNECTION_MESSAGE = "%s has left";

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;

    private final WebSocketMediator webSocketMediator;
    private final ChatStateService chatStateService;

    @Inject
    public ChannelServiceImpl(ProducerDelegate producerDelegate, ConsumerDelegate consumerDelegate,
            WebSocketMediator webSocketMediator, ChatStateService chatStateService) {
        this.producerDelegate = producerDelegate;
        this.consumerDelegate = consumerDelegate;
        this.webSocketMediator = webSocketMediator;
        this.chatStateService = chatStateService;
        initSubscribe();
    }

    private void initSubscribe() {
        // Subscribe on all channels "channel-.*"
        consumerDelegate.subscribe(ChannelNotice.TOPIC + ".*", ChannelNotice.class, packageMsg -> {
            LOGGER.debug("Receive new ChannelNotice");
            if (packageMsg.getNotice() == null || packageMsg.getNotice().getChannelId() == null) {
                LOGGER.error("Invalid ChannelNotice");
            } else {
                webSocketMediator.send(getSubscriptionId(packageMsg.getNotice().getChannelId()),
                        packageMsg.getNotice().getChannelMessage());
            }
        });
    }

    private String getSubscriptionId(String channelId) {
        return GeneralConst.SUBSCRIPTION_CHANNEL_PREFIX + channelId;
    }

    @Override
    public Promise<ChannelFull> connect(String userId, String channelId) {
        Promise<ChannelFull> promise = Promise.promise();
        LOGGER.info("User {} connect to {}", userId, channelId);
        String connectionMessage = String.format(CONNECTION_MESSAGE, userId);

        // 1. Get Channel (error if it doesn't exist)
        getChannel(channelId).future()
            .compose(channel ->
                // 2. Refresh subscribe topic
                consumerDelegate.refreshSubscribe(ChannelNotice.TOPIC + ".*").future().map(channel))
            .compose(channel ->
                // 3. Send connect message
                sendServerMessage(channelId, connectionMessage, MessageCode.CONNECT).future().map(channel))
            .compose(channel -> {
                // 4. Connect the webSocket and get last channel messages
                try {
                    webSocketMediator.connectUserForSubscription(userId, getSubscriptionId(channelId), true);
                    return getChannelMessages(channelId).future()
                            .map(channelMessages -> new ChannelFull(channel, channelMessages));
                } catch (InvalidStateException exception) {
                    Promise<ChannelFull> promiseError = Promise.promise();
                    promiseError.fail(exception);
                    return promiseError.future();
                }
            }).onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<Void> disconnect(String userId, String channelId) {
        Promise<Void> promise = Promise.promise();
        LOGGER.info("User {} disconnect to {}", userId, channelId);
        String disconnectionMessage = String.format(DISCONNECTION_MESSAGE, userId);

        sendServerMessage(channelId, disconnectionMessage, MessageCode.DISCONNECT).future()
            .compose(channelMessage -> Future.<Void>future(disconnectWebSocketPromise -> {
                try {
                    webSocketMediator.connectUserForSubscription(userId, getSubscriptionId(channelId), false);
                    disconnectWebSocketPromise.complete();
                } catch (InvalidStateException exception) {
                    disconnectWebSocketPromise.fail(exception);
                }
            }))
            .onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<Void> disconnectSubscription(String userId, String subscriptionId) {
        Promise<Void> promise = Promise.promise();

        if (!subscriptionId.startsWith(GeneralConst.SUBSCRIPTION_CHANNEL_PREFIX)) {
            LOGGER.debug("Disconnect subscription do nothing for subscription {}", subscriptionId);
            promise.complete();
            return promise;
        }

        LOGGER.info("Disconnect subscription {} for user {}", subscriptionId, userId);
        String channelId = subscriptionId.replace(GeneralConst.SUBSCRIPTION_CHANNEL_PREFIX, "");
        sendServerMessage(channelId, String.format(DISCONNECTION_MESSAGE, userId), MessageCode.DISCONNECT).future()
            .<Void>mapEmpty()
            .onComplete(promise::handle);

        return promise;
    }

    private Promise<Channel> getChannel(String channelId) {
        Promise<Channel> promise = Promise.promise();

        chatStateService.getChatStateInternal().future().onFailure(promise::fail).onSuccess(chatStateInternal -> {
            Channel channel = chatStateInternal.getChannels().get(channelId);
            if (channel == null) {
                promise.fail(new NotFoundException(String.format("Channel not found for id : %s", channelId)));
            } else {
                promise.complete(channel);
            }
        });

        return promise;
    }

    private Promise<List<ChannelMessage>> getChannelMessages(String channelId) {
        Promise<List<ChannelMessage>> promise = Promise.promise();

        String topic = ChannelNotice.getTopicForChannelId(channelId);
        consumerDelegate.getLastMessages(topic, GeneralConst.CHANNEL_MESSAGES_TO_SEND, ChannelNotice.class)
            .future()
            .map(channelNoticeList -> channelNoticeList.stream()
                .map(ChannelNotice::getChannelMessage)
                .collect(Collectors.toList()))
            .onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<ChannelFull> getChannelFull(String channelId) {
        Promise<ChannelFull> promise = Promise.promise();

        getChannel(channelId).future()
            .compose(channel -> getChannelMessages(channelId).future()
                .map(channelMessages -> new ChannelFull(channel, channelMessages)))
            .onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<ChannelMessage> sendUserMessage(String userId, String channelId, String message) {
        return sendMessage(userId, channelId, message, MessageCode.MSG);
    }

    @Override
    public Promise<ChannelMessage> sendServerMessage(String channelId, String message, MessageCode code) {
        return sendMessage(GeneralConst.SERVER_USERNAME, channelId, message, code);
    }

    private Promise<ChannelMessage> sendMessage(String userId, String channelId, String message, MessageCode code) {
        Promise<ChannelMessage> promise = Promise.promise();
        ChannelNotice channelNotice = new ChannelNotice();
        channelNotice.setChannelId(channelId);

        ChannelMessage channelMessage = new ChannelMessage();
        String messageId = UUID.randomUUID().toString();
        channelMessage.setId(messageId);
        channelMessage.setChannelId(channelId);
        channelMessage.setUser(userId);
        channelMessage.setMessage(message);
        channelMessage.setCode(code);
        channelMessage.setDate(new Date());
        channelNotice.setChannelMessage(channelMessage);

        LOGGER.info("Send message '{}' on the channel '{}' by '{}' (code: {})", messageId, channelId, userId, code);
        producerDelegate.publish(channelNotice)
            .future()
            .onFailure(promise::fail)
            .onSuccess(recordResult -> promise.complete(channelMessage));
        return promise;
    }

}
