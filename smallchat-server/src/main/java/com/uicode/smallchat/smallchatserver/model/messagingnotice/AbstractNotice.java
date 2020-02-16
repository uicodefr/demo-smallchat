package com.uicode.smallchat.smallchatserver.model.messagingnotice;

public abstract class AbstractNotice {

    private String topic;

    public AbstractNotice(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

}
