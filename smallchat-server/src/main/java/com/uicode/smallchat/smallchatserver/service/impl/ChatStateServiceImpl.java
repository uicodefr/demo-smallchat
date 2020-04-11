package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.LimitationException;
import com.uicode.smallchat.smallchatserver.exception.NotFoundException;
import com.uicode.smallchat.smallchatserver.messaging.AdminTopicDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.SubscriptionMsg;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.MessageCode;
import com.uicode.smallchat.smallchatserver.model.chat.AbstractStateEntity;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.model.chat.ChatUser;
import com.uicode.smallchat.smallchatserver.model.chat.internal.ChatStateInternal;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.ChannelNotice;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.ChatStateNotice;
import com.uicode.smallchat.smallchatserver.service.ChannelService;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMsg;

import io.vertx.core.Promise;

public class ChatStateServiceImpl implements ChatStateService {

    private static final Logger LOGGER = LogManager.getLogger(ChatStateServiceImpl.class);

    private static final Integer MAX_CHANNEL = 50;
    private static final String CREATED_CHANNEL_MESSAGE = "channel created";
    private static final String DELETED_CHANNEL_MESSAGE = "channel deleted";

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;
    private final AdminTopicDelegate adminTopicDelegate;

    private final ChannelService channelService;
    private final WebSocketMediator webSocketMediator;

    private final Promise<Void> initPromise;
    private SubscriptionMsg subscription;
    private ChatStateInternal chatStateInt;

    @Inject
    public ChatStateServiceImpl(ProducerDelegate producerDelegate, ConsumerDelegate consumerDelegate,
            AdminTopicDelegate adminTopicDelegate, ChannelService channelService, WebSocketMediator webSocketMediator) {
        this.producerDelegate = producerDelegate;
        this.consumerDelegate = consumerDelegate;
        this.adminTopicDelegate = adminTopicDelegate;
        this.channelService = channelService;
        this.webSocketMediator = webSocketMediator;

        this.initPromise = Promise.promise();
        this.getChatState().future().<Void>mapEmpty().onComplete(initPromise::handle);
    }

    @Override
    public Promise<Void> isInit() {
        return initPromise;
    }

    @Override
    public Promise<ChatState> getChatState() {
        Promise<ChatState> promise = Promise.promise();
        getChatStateInternal().future().map(ChatStateInternal::toChatState).onComplete(promise::handle);
        LOGGER.info("Get ChatState");
        return promise;
    }

    @Override
    public Promise<ChatStateInternal> getChatStateInternal() {
        Promise<ChatStateInternal> promise = Promise.promise();
        if (chatStateInt != null) {
            promise.complete(chatStateInt);
            return promise;
        }

        initSubscribe().future().onComplete(initResult -> {
            if (initResult.failed()) {
                LOGGER.error("Init subscribe for ChatState failed", initResult.cause());
                promise.fail(initResult.cause());
            } else {
                LOGGER.info("Init subscribe for ChatState succeeded");
                promise.complete(mergeChatState(chatStateInt));
            }
        });
        return promise;
    }

    private Promise<Void> initSubscribe() {
        Promise<Void> promise = Promise.promise();
        if (subscription != null) {
            promise.complete();
            return promise;
        }

        // First, create the topic if necessary
        adminTopicDelegate.createTopicIfNecessary(ChatStateNotice.TOPIC)
            .future()
            .onFailure(promise::fail)
            .onSuccess(creationTopicResult -> {
                // Then, subscribe
                subscription = consumerDelegate.subscribe(ChatStateNotice.TOPIC, ChatStateNotice.class, packageMsg -> {
                    LOGGER.trace("Receive new ChatState");
                    mergeChatState(packageMsg.getNotice().getChatState());
                    // Publish the new ChatState to the WebSocket
                    webSocketMediator.send(WebSocketMsg.CHAT_STATE_SUBJECT, chatStateInt.toChatState());

                }, subscribeCompletion ->
                // Finally, Resend Last Message after subscription
                consumerDelegate.resendLastMessages(ChatStateNotice.TOPIC, 1)
                    .future()
                    .<Void>mapEmpty()
                    .onComplete(promise::handle));
            });

        return promise;
    }

    private ChatStateInternal mergeChatState(ChatStateInternal chatStateToMerge) {
        if (chatStateInt == null) {
            chatStateInt = new ChatStateInternal();
        }
        if (chatStateToMerge != null) {
            mergeAbstractStateEntityList(chatStateInt.getChannels(), chatStateToMerge.getChannels().values());
            mergeAbstractStateEntityList(chatStateInt.getUsers(), chatStateToMerge.getUsers().values());
        }
        chatStateInt.setUpdateDate(new Date());
        return chatStateInt;
    }

    private static <T extends AbstractStateEntity> void mergeAbstractStateEntityList(Map<String, T> stateMap,
            Collection<T> stateEntityList) {
        for (T objectToMerge : stateEntityList) {
            T existingObject = stateMap.get(objectToMerge.getId());
            if (existingObject != null) {
                if (objectToMerge.isDelete()) {
                    // Delete
                    stateMap.remove(objectToMerge.getId());
                } else {
                    // Modify (replace)
                    stateMap.put(objectToMerge.getId(), objectToMerge);
                }
            } else if (!objectToMerge.isDelete()) {
                // Add
                stateMap.put(objectToMerge.getId(), objectToMerge);
            }
        }
    }

    private <T> Promise<T> changeChatState(Function<ChatStateInternal, T> action) {
        Promise<T> promise = Promise.promise();
        getChatStateInternal().future().onFailure(promise::fail).onSuccess(chatStateResult -> {
            ChatStateInternal newChatState = chatStateResult;
            T resultOnComplete = action.apply(newChatState);

            // Publish the newChatState on Kafka
            ChatStateNotice message = new ChatStateNotice();
            message.setChatState(newChatState);
            producerDelegate.publish(message)
                .future()
                .onFailure(promise::fail)
                .onSuccess(publishResult -> promise.complete(resultOnComplete));
        });
        return promise;
    }

    @Override
    public Promise<Channel> createChannel(Channel newChannel) {
        Promise<Channel> promise = Promise.promise();
        LOGGER.info("Create channel with id : {}", newChannel.getId());
        String topic = ChannelNotice.getTopicForChannelId(newChannel.getId());

        // First, Create the topic if necessary
        adminTopicDelegate.createTopicIfNecessary(topic).future().compose(creationTopicResult ->
        // Second, Change the chatState for creating the channel
        changeChatState(newChatState -> {
            if (newChatState.getChannels().size() > MAX_CHANNEL) {
                throw new LimitationException("Create channel failed because channels can not exceed " + MAX_CHANNEL);
            }

            newChannel.setDelete(false);
            newChatState.getChannels().put(newChannel.getId(), newChannel);
            return newChannel;
        }).future()).compose(newChatStateResult ->
        // Then, notify the creation of the channel
        channelService.sendServerMessage(newChannel.getId(), CREATED_CHANNEL_MESSAGE, MessageCode.CREATED)
            .future()
            .map(newChatStateResult)).onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<Void> deleteChannel(String channelId) {
        Promise<Void> promise = Promise.promise();
        LOGGER.info("Delete channel with id : {}", channelId);
        String topic = ChannelNotice.getTopicForChannelId(channelId);

        // First, Change the chatState for deleting the channel
        changeChatState(newChatState -> {
            Channel channelToDelete = new Channel();
            channelToDelete.setId(channelId);
            channelToDelete.setDelete(true);
            newChatState.getChannels().put(channelId, channelToDelete);
            return null;
        }).future().compose(chatStateResult ->
        // Second, notify the deletion of the channel
        channelService.sendServerMessage(channelId, DELETED_CHANNEL_MESSAGE, MessageCode.DELETED).future())
            .compose(sendResult ->
            // Then, Delete the topic
            adminTopicDelegate.deleteTopic(topic).future())
            .onComplete(promise::handle);

        return promise;
    }

    @Override
    public Promise<Channel> updateChannel(String channelId, Channel channel) {
        LOGGER.info("Update channel with id : {}", channelId);
        return changeChatState(newChatState -> {
            Channel channelToUpdate = newChatState.getChannels().get(channelId);
            if (channelToUpdate == null) {
                throw new NotFoundException(String.format("Channel not found for id : %s", channelId));
            }

            if (channel.getName() != null) {
                channelToUpdate.setName(channel.getName());
            }
            if (channel.getDescription() != null) {
                channelToUpdate.setDescription(channel.getDescription());
            }
            return channelToUpdate;
        });
    }

    @Override
    public void receiveUserConnection(String userId, boolean connection) {
        LOGGER.info("Received UserConnection in the chat : {} (connection : {})", userId, connection);
        ChatUser chatUser = new ChatUser();
        chatUser.setId(userId);
        chatUser.setDelete(!connection);

        changeChatState(newChatState -> {
            if (connection) {
                // Create if Absent
                if (StringUtils.isEmpty(chatUser.getPseudo())) {
                    chatUser.setPseudo(userId);
                }
                newChatState.getUsers().putIfAbsent(userId, chatUser);

            } else {
                // Delete
                newChatState.getUsers().put(userId, chatUser);
            }
            return null;
        });
    }

}
