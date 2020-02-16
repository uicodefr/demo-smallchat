package com.uicode.smallchat.smallchatserver.service;

import com.uicode.smallchat.smallchatserver.model.channel.ChannelFull;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage;

import io.vertx.core.Promise;

public interface ChannelService {

    Promise<ChannelFull> getChannel(String channelId);

    Promise<ChannelFull> connect(String userId, String channelId);

    Promise<Void> disconnect(String userId, String channelId);

    ChannelMessage sendMessage(String userId, String channelId, String message);

}
