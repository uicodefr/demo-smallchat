package com.uicode.smallchat.smallchatserver.messaging;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class SubscriptionMsg {

    private MessageConsumer<JsonObject> messageConsumer;

    public SubscriptionMsg(MessageConsumer<JsonObject> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public void unsubscribe() {
        messageConsumer.unregister();
    }

}
