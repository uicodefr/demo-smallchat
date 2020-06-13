package com.uicode.smallchat.smallchatserver.websocket.impl;

public class SendChannelMessageMsg {

    private String channelId;

    private String message;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
