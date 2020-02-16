package com.uicode.smallchat.smallchatserver.messaging;

import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class PackageMsgNotice<T extends AbstractNotice> {

    private T notice;

    private MessageConsumer<JsonObject> consumer;

    public PackageMsgNotice(T notice, MessageConsumer<JsonObject> consumer) {
        this.notice = notice;
        this.consumer = consumer;
    }

    public T getNotice() {
        return notice;
    }

    public void unsubscribe() {
        this.consumer.unregister();
    }

}
