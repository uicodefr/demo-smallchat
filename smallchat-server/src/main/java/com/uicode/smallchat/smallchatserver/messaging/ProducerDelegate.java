package com.uicode.smallchat.smallchatserver.messaging;

import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;

import io.vertx.core.Promise;
import io.vertx.kafka.client.producer.RecordMetadata;

public interface ProducerDelegate {

    Promise<RecordMetadata> publish(AbstractNotice message);

}
