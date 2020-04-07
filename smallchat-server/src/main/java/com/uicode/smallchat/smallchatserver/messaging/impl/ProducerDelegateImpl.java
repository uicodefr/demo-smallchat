package com.uicode.smallchat.smallchatserver.messaging.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;

public class ProducerDelegateImpl implements ProducerDelegate {

    private static final Logger LOGGER = LogManager.getLogger(ProducerDelegateImpl.class);

    private final Vertx vertx;
    private KafkaProducer<String, JsonObject> globalProducer;

    @Inject
    public ProducerDelegateImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    private void initProducer() {
        if (globalProducer != null) {
            return;
        }
        globalProducer = KafkaProducer.create(vertx, ConfigUtil.getConfig().getKafkaProducer());
    }

    @Override
    public Promise<RecordMetadata> publish(AbstractNotice message) {
        Promise<RecordMetadata> promise = Promise.promise();
        this.initProducer();

        JsonObject messageJson = JsonObject.mapFrom(message);
        KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(message.getTopic(), messageJson);
        globalProducer.send(record, promise::handle);

        promise.future().onFailure(error -> LOGGER.error("Error when publishing", error));
        return promise;
    }

}
