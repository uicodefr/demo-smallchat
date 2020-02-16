package com.uicode.smallchat.smallchatserver.model.channel;

import java.util.List;

import com.uicode.smallchat.smallchatserver.model.chat.Channel;

public class ChannelFull extends Channel {

    private List<ChannelMessage> messages;

    public ChannelFull(Channel channel, List<ChannelMessage> messages) {
        super(channel);
        this.messages = messages;
    }

    public List<ChannelMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChannelMessage> messages) {
        this.messages = messages;
    }

}
