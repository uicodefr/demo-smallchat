package com.uicode.smallchat.smallchatserver.messaging;

import com.uicode.smallchat.smallchatserver.model.message.AbstractMessage;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class PackageMsg<T extends AbstractMessage> {

    private T message;

    private MessageConsumer<JsonObject> consumer;

    public PackageMsg(T message, MessageConsumer<JsonObject> consumer) {
        this.message = message;
        this.consumer = consumer;
    }

    public T getMessage() {
        return message;
    }

    public void unsubscribe() {
        this.consumer.unregister();
    }

}
