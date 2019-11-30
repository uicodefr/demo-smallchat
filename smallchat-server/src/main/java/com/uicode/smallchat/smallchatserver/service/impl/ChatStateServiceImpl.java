package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.NotFoundException;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.SubscriptionMsg;
import com.uicode.smallchat.smallchatserver.model.chat.AbstractStateEntity;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.model.chat.ChatUser;
import com.uicode.smallchat.smallchatserver.model.chat.internal.ChatStateInternal;
import com.uicode.smallchat.smallchatserver.model.message.ChatStateMessage;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;

import io.vertx.core.Promise;

public class ChatStateServiceImpl implements ChatStateService {

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;

    private final WebSocketMediator webSocketMediator;

    private SubscriptionMsg subscription;

    private ChatStateInternal chatStateInt;

    @Inject
    public ChatStateServiceImpl(
        ProducerDelegate producerDelegate,
        ConsumerDelegate consumerDelegate,
        WebSocketMediator webSocketMediator
    ) {
        this.producerDelegate = producerDelegate;
        this.consumerDelegate = consumerDelegate;
        this.webSocketMediator = webSocketMediator;
        this.getChatState();
    }

    @Override
    public Promise<ChatState> getChatState() {
        Promise<ChatState> promise = Promise.promise();
        getChatStateInternal().future().map(ChatStateInternal::toChatState).setHandler(promise::handle);
        return promise;
    }

    private Promise<ChatStateInternal> getChatStateInternal() {
        Promise<ChatStateInternal> promise = Promise.promise();
        if (chatStateInt != null) {
            promise.complete(chatStateInt);
            return promise;
        }

        initSubscribe().future().setHandler(initResult -> {
            if (initResult.failed()) {
                promise.fail(initResult.cause());
            }
            promise.complete(mergeChatState(chatStateInt));
        });
        return promise;
    }

    private Promise<Void> initSubscribe() {
        Promise<Void> promise = Promise.promise();
        if (subscription != null) {
            promise.complete();
            return promise;
        }

        subscription = consumerDelegate.subscribe(ChatStateMessage.TOPIC, ChatStateMessage.class, packageMsg -> {
            mergeChatState(packageMsg.getMessage().getChatState());
            // Publish the new ChatState to the Websocket
            webSocketMediator.send(GeneralConst.CHAT_STATE_CHANNEL, chatStateInt.toChatState());
        });

        consumerDelegate.resendLastMessage(ChatStateMessage.TOPIC, 1).future().setHandler(resendResult -> {
            if (resendResult.failed()) {
                promise.fail(resendResult.cause());
            } else {
                promise.complete();
            }
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
        getChatStateInternal().future().setHandler(chatStateResult -> {
            if (chatStateResult.failed()) {
                promise.fail(chatStateResult.cause());
                return;
            }

            ChatStateInternal newChatState = chatStateResult.result();
            T resultOnComplete = action.apply(newChatState);

            // Publish the newChatState on Kafka
            ChatStateMessage message = new ChatStateMessage();
            message.setChatState(newChatState);
            producerDelegate.publish(message).future().setHandler(publishResult -> {
                if (publishResult.failed()) {
                    promise.fail(publishResult.cause());
                } else {
                    promise.complete(resultOnComplete);
                }
            });
        });
        return promise;
    }

    @Override
    public Promise<Channel> createChannel(Channel newChannel) {
        return changeChatState(newChatState -> {
            newChannel.setDelete(false);
            newChatState.getChannels().put(newChannel.getId(), newChannel);
            return newChannel;
        });
    }

    @Override
    public Promise<Void> deleteChannel(String channelId) {
        return changeChatState(newChatState -> {
            Channel channelToDelete = new Channel();
            channelToDelete.setId(channelId);
            channelToDelete.setDelete(true);
            newChatState.getChannels().put(channelId, channelToDelete);
            return null;
        });
    }

    @Override
    public Promise<Channel> updateChannel(String channelId, Channel channel) {
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
