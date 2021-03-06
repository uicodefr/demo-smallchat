package com.uicode.smallchat.smallchatserver.messaging.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.PackageMsgNotice;
import com.uicode.smallchat.smallchatserver.messaging.SubscriptionMsg;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;

public class ConsumerDelegateImpl implements ConsumerDelegate {

    private static final Logger LOGGER = LogManager.getLogger(ConsumerDelegateImpl.class);

    private static final String PREFIX_EVENT_BUS = "consumer.";

    private final Vertx vertx;
    private Map<String, KafkaConsumer<String, JsonObject>> globalConsumerMap = new HashMap<>();

    @Inject
    public ConsumerDelegateImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    private KafkaConsumer<String, JsonObject> getConsumer(String topic) {
        return globalConsumerMap.computeIfAbsent(topic, key -> {
            KafkaConsumer<String, JsonObject> newConsumer = KafkaConsumer.create(vertx,
                    ConfigUtil.getConfig().getKafkaConsumer());
            newConsumer.exceptionHandler(error -> LOGGER.error("Error with consumer", error));
            newConsumer.handler(consumerRecord -> this.handleConsumer(topic, consumerRecord));
            return newConsumer;
        });
    }

    private void handleConsumer(String topic, KafkaConsumerRecord<String, JsonObject> consumerRecord) {
        LOGGER.trace("Received new record from topic {} (offset {}, partition {})", consumerRecord.topic(),
                consumerRecord.offset(), consumerRecord.partition());
        vertx.eventBus().publish(PREFIX_EVENT_BUS + topic, consumerRecord.value());
    }

    @Override
    public <T extends AbstractNotice> SubscriptionMsg subscribe(String topic, Class<T> type,
            Handler<PackageMsgNotice<T>> receiveHandler) {
        return subscribe(topic, type, receiveHandler, subscribeResult -> {
            if (subscribeResult.failed()) {
                LOGGER.error("Error when subscribing", subscribeResult.cause());
            }
        });
    }

    @Override
    public <T extends AbstractNotice> SubscriptionMsg subscribe(String topic, Class<T> type,
            Handler<PackageMsgNotice<T>> receiveHandler, Handler<AsyncResult<Void>> completionHandler) {
        KafkaConsumer<String, JsonObject> consumerForTopic = this.getConsumer(topic);
        consumerForTopic.subscribe(Pattern.compile(topic), completionHandler);

        MessageConsumer<JsonObject> subscribeConsumer = vertx.eventBus().consumer(PREFIX_EVENT_BUS + topic);
        subscribeConsumer.handler(message -> {
            PackageMsgNotice<T> packageMsg = new PackageMsgNotice<>(message.body().mapTo(type), subscribeConsumer);
            receiveHandler.handle(packageMsg);
        });
        return new SubscriptionMsg(subscribeConsumer);
    }

    @Override
    public Promise<Void> refreshSubscribe(String topic) {
        Promise<Void> promise = Promise.promise();

        KafkaConsumer<String, JsonObject> consumerForTopic = this.getConsumer(topic);
        consumerForTopic.subscribe(Pattern.compile(topic), subscribeResult -> {
            if (subscribeResult.failed()) {
                LOGGER.error("Error when refresh subscribing", subscribeResult.cause());
            }
            promise.handle(subscribeResult);
        });

        return promise;
    }

    @Override
    public <T extends AbstractNotice> Promise<T> poll(String topic, Class<T> type, long timeout) {
        Promise<T> promise = Promise.promise();
        getConsumer(topic).poll(Duration.ofMillis(timeout), pollResult -> {
            if (pollResult.failed()) {
                promise.fail(pollResult.cause());
                return;
            }

            JsonObject resultJson = null;
            for (ConsumerRecord<String, JsonObject> record : pollResult.result().records()) {
                if (record.topic().startsWith(topic)) {
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
    public Promise<Void> resendLastMessages(String topic, int messageToResend) {
        Promise<Void> promise = Promise.promise();
        KafkaConsumer<String, JsonObject> consumerForTopic = this.getConsumer(topic);

        changePosition(consumerForTopic, topic, messageToResend).future().<Void>mapEmpty().onComplete(result -> {
            if (result.failed()) {
                LOGGER.error("Resend Last Message Failed", result.cause());
                promise.fail(result.cause());
                return;
            }
            promise.complete();
        });

        return promise;
    }

    @Override
    public <T extends AbstractNotice> Promise<List<T>> getLastMessages(String topic, int messagesToGet, Class<T> type) {
        Promise<List<T>> resultPromise = Promise.promise();

        String anonymousGroupId = "anonymous." + UUID.randomUUID();

        Map<String, String> kafkaConfig = new HashMap<>(ConfigUtil.getConfig().getKafkaConsumer());
        kafkaConfig.put("group.id", anonymousGroupId);
        KafkaConsumer<String, JsonObject> anonymousConsumer = KafkaConsumer.create(vertx, kafkaConfig);
        anonymousConsumer
            .exceptionHandler(error -> LOGGER.error("Error with anonymousConsumer on getLastMessages", error));

        List<T> resultList = new ArrayList<>();

        Future.<Void>future(subscribePromise -> {
            // 1. Subscribe
            LOGGER.debug("getLastMessages - 1. subscribe");
            anonymousConsumer.subscribe(topic, subscribePromise::handle);
        }).compose(subscribeDone -> {
            // 2. Change the position
            LOGGER.debug("getLastMessages - 2. change the position");
            return Future.<Map<TopicPartition, Long>>future(
                    changePositionPromise -> changePosition(anonymousConsumer, topic, messagesToGet).future()
                        .onComplete(changePositionPromise));

        }).compose(changePositionResult -> {
            // 3. Check current position with the end position
            // To Remove Empty TopicPartition
            LOGGER.debug("getLastMessages - 3. check changePosition");
            return checkChangePositionResult(anonymousConsumer, changePositionResult).future();

        }).onFailure(resultPromise::fail).onSuccess(checkPositionResult -> {
            if (checkPositionResult.isEmpty()) {
                resultPromise.complete(resultList);
                return;
            }

            Map<TopicPartition, Long> endPositionForTopics = checkPositionResult;

            // 4. Receive Messages and add it to the list
            // and complete when all messages are received
            LOGGER.debug("getLastMessages - 4. receive messages");
            anonymousConsumer.handler(consumerRecord -> {
                resultList.add(consumerRecord.value().mapTo(type));

                TopicPartition recordPartition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
                long offsetPosition = consumerRecord.offset();
                Long endPosition = endPositionForTopics.get(recordPartition);
                if (endPosition != null && endPosition.longValue() <= offsetPosition + 1) {
                    endPositionForTopics.remove(recordPartition);
                }

                if (endPositionForTopics.isEmpty()) {
                    resultPromise.complete(resultList);
                    return;
                }
            });
        });

        resultPromise.future().onComplete(result ->
        // Close Consumer finally (on success or on error)
        anonymousConsumer.close());

        return resultPromise;
    }

    private static Promise<Map<TopicPartition, Long>> changePosition(KafkaConsumer<String, JsonObject> consumer,
            String topic, int messagesToRewind) {
        Promise<Map<TopicPartition, Long>> promise = Promise.promise();

        Future.<KafkaConsumerRecords<String, JsonObject>>future(pollPromise ->
        // XXX Pool with 100ms for establish a connection and load assignment
        consumer.poll(Duration.ofMillis(100), pollPromise::handle)

        ).compose(consumerRecords ->
        // Get Topic Partitions
        Future.<Set<TopicPartition>>future(topicsPromise -> consumer.assignment(topicsPromise::handle))

        ).compose(topicPartitionSet ->
        // Change topic position and get end position for each TopicPartitions
        Future.<Map<TopicPartition, Long>>future(changeTopicPromise -> changePositionForPartitions(consumer,
                changeTopicPromise, topic, messagesToRewind, topicPartitionSet))

        ).onComplete(promise::handle);

        return promise;
    }

    private static void changePositionForPartitions(KafkaConsumer<String, JsonObject> consumer,
            Promise<Map<TopicPartition, Long>> finalPromise, String topic, int messagesToRewind,
            Set<TopicPartition> topicPartitionSet) {
        Map<TopicPartition, Long> topicPartitionWithEndPosition = new HashMap<>();

        @SuppressWarnings("rawtypes")
        List<Future> compositeFutures = new ArrayList<>();

        for (TopicPartition topicPartition : topicPartitionSet) {
            if (!topicPartition.getTopic().startsWith(topic)) {
                continue;
            }

            Future<Void> localFuture = Future.<Long>future(endPositionPromise -> {
                // Get End Offset
                LOGGER.debug("changePositionForPartitions - get end offset");
                consumer.endOffsets(topicPartition, endPositionPromise::handle);

            }).compose(endPosition -> Future.<Pair<Long, Long>>future(pairPositionPromise -> {
                // Get Beginning Offset
                LOGGER.debug("changePositionForPartitions - get beginning offset");
                consumer.beginningOffsets(topicPartition, beginPositionHandler -> pairPositionPromise
                    .handle(beginPositionHandler.map(beginPosition -> Pair.of(beginPosition, endPosition))));

            })).compose(pairPosition ->
            // Change the position for the consumer
            Future.<Void>future(seekPromise -> {
                Long beginPosition = pairPosition.getLeft();
                Long endPosition = pairPosition.getRight();
                LOGGER.trace("changePositionForPartitions for topic {} - beginPosition : {} -  endPosition : {}", topic,
                        beginPosition, endPosition);

                topicPartitionWithEndPosition.put(topicPartition, endPosition);
                long newPosition = Math.max(beginPosition, endPosition - messagesToRewind);
                consumer.seek(topicPartition, newPosition, seekPromise::handle);
            }));

            compositeFutures.add(localFuture);
        }

        CompositeFuture.all(compositeFutures)
            .onFailure(finalPromise::fail)
            .onSuccess(compositeResult -> finalPromise.complete(topicPartitionWithEndPosition));
    }

    private Promise<Map<TopicPartition, Long>> checkChangePositionResult(KafkaConsumer<String, JsonObject> consumer,
            Map<TopicPartition, Long> changePositionResult) {
        Promise<Map<TopicPartition, Long>> checkedPositionPromise = Promise.promise();

        @SuppressWarnings("rawtypes")
        List<Future> compositeFutures = new ArrayList<>();
        Map<TopicPartition, Long> checkPositionResult = new HashMap<>(changePositionResult);

        for (Entry<TopicPartition, Long> changePositionEntry : changePositionResult.entrySet()) {
            Promise<Void> checkPositionPromise = Promise.promise();
            compositeFutures.add(checkPositionPromise.future());

            TopicPartition topicPartition = changePositionEntry.getKey();
            consumer.position(topicPartition, positionResult -> {
                if (positionResult.failed()) {
                    checkPositionPromise.fail(positionResult.cause());
                    return;
                }

                // Filter the TopicPartition with a current position equals to endPosition
                // This check should normally be used to only handle empty topic
                Long endPosition = changePositionEntry.getValue();
                if (endPosition <= positionResult.result()) {
                    checkPositionResult.remove(topicPartition);
                }
                checkPositionPromise.complete();
            });
        }

        CompositeFuture.all(compositeFutures)
            .onFailure(checkedPositionPromise::fail)
            .onSuccess(compositeResult -> checkedPositionPromise.complete(checkPositionResult));

        return checkedPositionPromise;
    }

}
