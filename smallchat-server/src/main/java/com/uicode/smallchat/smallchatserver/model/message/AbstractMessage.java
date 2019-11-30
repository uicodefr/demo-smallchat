package com.uicode.smallchat.smallchatserver.model.message;

public abstract class AbstractMessage {

    private String topic;

    public AbstractMessage(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

}
