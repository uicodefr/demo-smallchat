package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.NotFoundException;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelFull;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.Type;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.ChannelNotice;
import com.uicode.smallchat.smallchatserver.service.ChannelService;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class ChannelServiceImpl implements ChannelService {

    private static final Logger LOGGER = LogManager.getLogger(ChannelServiceImpl.class);

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;

    private final WebSocketMediator webSocketMediator;
    private final ChatStateService chatStateService;

    @Inject
    public ChannelServiceImpl(
        ProducerDelegate producerDelegate,
        ConsumerDelegate consumerDelegate,
        WebSocketMediator webSocketMediator,
        ChatStateService chatStateService
    ) {
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
        consumerDelegate.refreshSubscribe(ChannelNotice.TOPIC + ".*");

        LOGGER.info("User {} connect to {}", userId, channelId);
        webSocketMediator.connectUserForSubscription(userId, getSubscriptionId(channelId), true);
        return getChannel(channelId);
    }

    @Override
    public Promise<Void> disconnect(String userId, String channelId) {
        LOGGER.info("User {} disconnect to {}", userId, channelId);
        Promise<Void> promise = Promise.promise();
        webSocketMediator.connectUserForSubscription(userId, getSubscriptionId(channelId), false);
        promise.complete();
        return promise;
    }

    @Override
    public Promise<ChannelFull> getChannel(String channelId) {
        Promise<ChannelFull> promise = Promise.promise();

        Future<Channel> channelFuture = chatStateService.getChatStateInternal().future()
            .map(chatStateInternal -> chatStateInternal.getChannels().get(channelId));

        String topic = ChannelNotice.getTopicForChannelId(channelId);
        Future<List<ChannelMessage>> messagesFuture = consumerDelegate
            .getLastMessages(topic, GeneralConst.CHANNEL_MESSAGES_TO_SEND, ChannelNotice.class).future()
            .map(channelNoticeList -> channelNoticeList.stream().map(ChannelNotice::getChannelMessage)
                    .collect(Collectors.toList()));

        CompositeFuture.all(channelFuture, messagesFuture).onComplete(compositeResult -> {
            if (compositeResult.failed()) {
                promise.fail(compositeResult.cause());
                return;
            }
            if (channelFuture.result() == null) {
                promise.fail(new NotFoundException(String.format("Channel not found for id : %s", channelId)));
                return;
            }

            ChannelFull channelFull = new ChannelFull(channelFuture.result(), messagesFuture.result());
            promise.complete(channelFull);
        });

        return promise;
    }

    @Override
    public ChannelMessage sendMessage(String userId, String channelId, String message) {
        ChannelNotice channelNotice = new ChannelNotice();
        channelNotice.setChannelId(channelId);

        ChannelMessage channelMessage = new ChannelMessage();
        String messageId = UUID.randomUUID().toString();
        channelMessage.setId(messageId);
        channelMessage.setChannelId(channelId);
        channelMessage.setUser(userId);
        channelMessage.setMessage(message);
        channelMessage.setDate(new Date());
        channelMessage.setType(Type.MESSAGE);
        channelNotice.setChannelMessage(channelMessage);

        LOGGER.info("Send message '{}' on the channel '{}' by '{}'", messageId, channelId, userId);
        producerDelegate.publish(channelNotice);
        return channelMessage;
    }

}
