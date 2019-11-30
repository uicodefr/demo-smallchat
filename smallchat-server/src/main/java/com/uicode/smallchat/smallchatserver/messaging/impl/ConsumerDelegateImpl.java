package com.uicode.smallchat.smallchatserver.messaging.impl;

import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.PackageMsg;
import com.uicode.smallchat.smallchatserver.messaging.SubscriptionMsg;
import com.uicode.smallchat.smallchatserver.model.message.AbstractMessage;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

public class ConsumerDelegateImpl implements ConsumerDelegate {

    private static final String PREFIX_EVENT_BUS = "consumer.";

    private final Vertx vertx;
    private KafkaConsumer<String, JsonObject> globalConsumer;

    @Inject
    public ConsumerDelegateImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    private void initGlobalConsumer() {
        if (globalConsumer != null) {
            return;
        }
        globalConsumer = KafkaConsumer.create(vertx, ConfigUtil.getConfig().getKafkaConsumer());
        globalConsumer.handler(this::handleConsumer);
    }

    private void handleConsumer(KafkaConsumerRecord<String, JsonObject> consumerRecord) {
        vertx.eventBus().publish(PREFIX_EVENT_BUS + consumerRecord.topic(), consumerRecord.value());
    }

    @Override
    public <T extends AbstractMessage> SubscriptionMsg subscribe(String topic, Class<T> type,
            Handler<PackageMsg<T>> handler) {
        this.initGlobalConsumer();
        globalConsumer.subscribe(topic);

        MessageConsumer<JsonObject> subscribeConsumer = vertx.eventBus().consumer(PREFIX_EVENT_BUS + topic);
        subscribeConsumer.handler(message -> {
            PackageMsg<T> packageMsg = new PackageMsg<>(message.body().mapTo(type), subscribeConsumer);
            handler.handle(packageMsg);
        });
        return new SubscriptionMsg(subscribeConsumer);
    }

    @Override
    public <T extends AbstractMessage> Promise<T> poll(String topic, Class<T> type, long timeout) {
        Promise<T> promise = Promise.promise();
        globalConsumer.poll(timeout, pollResult -> {
            if (pollResult.failed()) {
                promise.fail(pollResult.cause());
                return;
            }

            JsonObject resultJson = null;
            for (ConsumerRecord<String, JsonObject> record : pollResult.result().records()) {
                if (record.topic().equals(topic)) {
                    resultJson = record.value();
                }
            }

            if (resultJson == null) {
                promise.complete(null);
            } else {
                promise.complete(resultJson.mapTo(type));
            }
        });
        return promise;
    }

    @Override
    public Promise<Void> resendLastMessage(String topic, int messageToResend) {
        Promise<Void> promise = Promise.promise();
        this.initGlobalConsumer();

        // Pool with 0 for establish a connection and load assignment
        globalConsumer.poll(0, pollResult -> {
            if (pollResult.failed()) {
                promise.fail(pollResult.cause());
                return;
            }

            globalConsumer.assignment(topicsResult -> {
                if (topicsResult.failed()) {
                    promise.fail(topicsResult.cause());
                    return;
                }

                changeTopicPosition(promise, topic, messageToResend, topicsResult.result());
            });
        });
        return promise;
    }

    private void changeTopicPosition(Promise<Void> promise, String topic, int messageToResend,
            Set<TopicPartition> topicPartitionSet) {

        for (TopicPartition topicPartition : topicPartitionSet) {
            if (!topicPartition.getTopic().equals(topic)) {
                continue;
            }

            globalConsumer.position(topicPartition, positionResult -> {
                if (positionResult.failed()) {
                    promise.fail(positionResult.cause());
                    return;
                }

                long newPosition = Math.max(0, positionResult.result() - messageToResend);
                globalConsumer.seek(topicPartition, newPosition);
                promise.tryComplete();
            });
        }
    }

}
