package com.uicode.smallchat.smallchatserver.service;

import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.model.chat.internal.ChatStateInternal;

import io.vertx.core.Promise;

public interface ChatStateService {

    Promise<ChatState> getChatState();

    Promise<ChatStateInternal> getChatStateInternal();

    Promise<Channel> createChannel(Channel newChannel);

    Promise<Void> deleteChannel(String channelId);

    Promise<Channel> updateChannel(String channelId, Channel channel);

    void receiveUserConnection(String userId, boolean connection);

}
