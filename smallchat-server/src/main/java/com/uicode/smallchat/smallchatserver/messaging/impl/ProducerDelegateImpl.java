package com.uicode.smallchat.smallchatserver.messaging.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;

public class ProducerDelegateImpl implements ProducerDelegate {

    private static final Logger LOGGER = LogManager.getLogger(ProducerDelegateImpl.class);

    private static final int TOPIC_NUM_PARTITION = 4;
    private static final short TOPIC_REPLICATION_FACTOR = 1;

    private final Vertx vertx;

    private KafkaAdminClient kafkaAdmin;
    private Set<String> createdTopics;

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
        kafkaAdmin = KafkaAdminClient.create(vertx, ConfigUtil.getConfig().getKafkaAdmin());
        createdTopics = new HashSet<>();
    }

    private Promise<Void> createTopic(String topic) {
        Promise<Void> promise = Promise.promise();
        // We check before in the list "createdTopics"
        if (createdTopics.contains(topic)) {
            promise.complete();
            return promise;
        }

        NewTopic newTopic = new NewTopic(topic, TOPIC_NUM_PARTITION, TOPIC_REPLICATION_FACTOR);
        kafkaAdmin.createTopics(Collections.singletonList(newTopic), creationHandler -> {
            // TopicExistsException is a error considered as normal
            if (creationHandler.failed() && !(creationHandler.cause() instanceof TopicExistsException)) {
                promise.fail(creationHandler.cause());
            } else {
                createdTopics.add(topic);
                promise.complete();
            }
        });
        return promise;
    }

    @Override
    public Promise<RecordMetadata> publish(AbstractNotice message) {
        Promise<RecordMetadata> promise = Promise.promise();
        this.initProducer();

        createTopic(message.getTopic()).future().onComplete(creationResult -> {
            if (creationResult.failed()) {
                promise.fail(creationResult.cause());
                return;
            }

            JsonObject messageJson = JsonObject.mapFrom(message);
            KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create(message.getTopic(),
                    messageJson);
            globalProducer.send(record, promise::handle);
        });
        
        promise.future().onComplete(publishResult -> {
            if (publishResult.failed()) {
                LOGGER.error("Error when publishing", publishResult.cause());
            }
        });
        
        return promise;
    }

}
