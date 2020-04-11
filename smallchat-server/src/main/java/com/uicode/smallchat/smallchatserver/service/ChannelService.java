package com.uicode.smallchat.smallchatserver.service;

import com.uicode.smallchat.smallchatserver.model.channel.ChannelFull;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.MessageCode;

import io.vertx.core.Promise;

public interface ChannelService {

    Promise<ChannelFull> getChannelFull(String channelId);

    Promise<ChannelFull> connect(String userId, String channelId);

    Promise<Void> disconnect(String userId, String channelId);

    Promise<Void> disconnectSubscription(String userId, String subscriptionId);

    Promise<ChannelMessage> sendUserMessage(String userId, String channelId, String message);

    Promise<ChannelMessage> sendServerMessage(String channelId, String message, MessageCode code);


}
