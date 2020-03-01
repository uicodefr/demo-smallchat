package com.uicode.smallchat.smallchatserver.model.messagingnotice;

import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage;

public class ChannelNotice extends AbstractNotice {

    public static final String TOPIC= "channel-";

    private String channelId;

    private ChannelMessage channelMessage;

    public ChannelNotice() {
        super(TOPIC);
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ChannelMessage getChannelMessage() {
        return channelMessage;
    }

    public void setChannelMessage(ChannelMessage channelMessage) {
        this.channelMessage = channelMessage;
    }

    @Override
    public String getTopic() {
        return TOPIC + channelId;
    }

    public static String getTopicForChannelId(String channelId) {
        return TOPIC + channelId;
    }

}
