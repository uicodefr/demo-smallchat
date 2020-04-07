package com.uicode.smallchat.smallchatserver.messaging;

import io.vertx.core.Promise;

public interface AdminTopicDelegate {

    Promise<Void> createTopicIfNecessary(String topic);

    Promise<Void> deleteTopic(String topic);

}
