package com.uicode.smallchat.smallchatserver.messaging.impl;

import java.util.Collections;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.messaging.AdminTopicDelegate;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;

public class AdminTopicDelegateImpl implements AdminTopicDelegate {

    private static final Logger LOGGER = LogManager.getLogger(AdminTopicDelegateImpl.class);

    private static final int TOPIC_NUM_PARTITION = 1;
    private static final short TOPIC_REPLICATION_FACTOR = 1;

    private final Vertx vertx;
    private KafkaAdminClient kafkaAdmin;

    @Inject
    public AdminTopicDelegateImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    private void initAdmin() {
        if (kafkaAdmin != null) {
            return;
        }
        kafkaAdmin = KafkaAdminClient.create(vertx, ConfigUtil.getConfig().getKafkaAdmin());
    }

    @Override
    public Promise<Void> createTopicIfNecessary(String topic) {
        this.initAdmin();
        Promise<Void> promise = Promise.promise();

        kafkaAdmin.listTopics(listTopicsHandler -> {
            if (listTopicsHandler.failed()) {
                promise.fail(listTopicsHandler.cause());
                return;
            }

            if (listTopicsHandler.result().contains(topic)) {
                LOGGER.debug("Existing topic {} found in createTopicIfNecessary", topic);
                promise.complete();
                return;
            }

            NewTopic newTopic = new NewTopic(topic, TOPIC_NUM_PARTITION, TOPIC_REPLICATION_FACTOR);
            kafkaAdmin.createTopics(Collections.singletonList(newTopic), creationHandler -> {
                // TopicExistsException is not a severe error
                if (creationHandler.failed() && !(creationHandler.cause() instanceof TopicExistsException)) {
                    promise.fail(creationHandler.cause());
                } else {
                    LOGGER.info("Create topic {}", topic);
                    promise.complete();
                }
            });
        });

        promise.future().onFailure(error -> LOGGER.error("Error with creatingTopicIfNecessary", error));
        return promise;
    }

    @Override
    public Promise<Void> deleteTopic(String topic) {
        this.initAdmin();
        Promise<Void> promise = Promise.promise();

        LOGGER.info("Delete topic {}", topic);
        kafkaAdmin.deleteTopics(Collections.singletonList(topic), promise::handle);

        promise.future().onFailure(error -> LOGGER.error("Error when deleting topic", error));
        return promise;
    }

}
