package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.dao.global.LikeDao;
import com.uicode.smallchat.smallchatserver.dao.global.ParameterDao;
import com.uicode.smallchat.smallchatserver.messaging.AdminTopicDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.model.IdLongEntity;
import com.uicode.smallchat.smallchatserver.model.global.CountLikes;
import com.uicode.smallchat.smallchatserver.model.global.GlobalStatus;
import com.uicode.smallchat.smallchatserver.model.messagingnotice.TestingNotice;
import com.uicode.smallchat.smallchatserver.service.GlobalService;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;
import com.uicode.smallchat.smallchatserver.util.parameter.ParameterConst;
import com.uicode.smallchat.smallchatserver.util.parameter.ParameterUtil;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class GlobalServiceImpl implements GlobalService {

    private static final Logger LOGGER = LogManager.getLogger(GlobalServiceImpl.class);

    private static final String VERSION = "0.1.10-SNAPSHOT";
    private static final Date UPDATE = new Date();

    private final ParameterDao parameterDao;
    private final LikeDao likeDao;

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;
    private final AdminTopicDelegate adminTopicDelegate;

    @Inject
    public GlobalServiceImpl(ParameterDao parameterDao, LikeDao likeDao, ProducerDelegate producerDelegate,
            ConsumerDelegate consumerDelegate, AdminTopicDelegate adminTopicDelegate) {
        this.parameterDao = parameterDao;
        this.likeDao = likeDao;

        this.producerDelegate = producerDelegate;
        this.consumerDelegate = consumerDelegate;
        this.adminTopicDelegate = adminTopicDelegate;

        // XXX Prepare the topic test because it doesn't work without initialization
        adminTopicDelegate.createTopicIfNecessary(TestingNotice.TOPIC)
            .future()
            .compose(nothing -> consumerDelegate.refreshSubscribe(TestingNotice.TOPIC).future());
    }

    @Override
    public Promise<GlobalStatus> getStatus() {
        Promise<GlobalStatus> promise = Promise.promise();
        GlobalStatus status = new GlobalStatus();
        status.setConfId(ConfigUtil.getConfig().getConfId());
        status.setUpDate(UPDATE);
        status.setCurrentDate(new Date());
        status.setVersion(VERSION);

        // Test Messaging (kafka) and test parameterDao
        Future<String> testMessagingFuture = testMessaging().future();
        Future<Optional<String>> testDaoFuture = parameterDao.getParameterValue(ParameterConst.GENERAL_STATUS).future();
        CompositeFuture.all(testMessagingFuture, testDaoFuture).onFailure(promise::fail).onSuccess(compositeResult -> {
            status.setMessaging(testMessagingFuture.result());
            testDaoFuture.result().ifPresent(status::setStatus);
            promise.complete(status);
        });

        LOGGER.info("Global Status asked");
        return promise;
    }

    private Promise<String> testMessaging() {
        Promise<String> promise = Promise.promise();

        // 1. Create topic if necessary
        adminTopicDelegate.createTopicIfNecessary(TestingNotice.TOPIC)
            .future()
            .onFailure(promise::fail)
            .onSuccess(creationTopicResult -> {
                // 2. Then, subscribe
                consumerDelegate.subscribe(TestingNotice.TOPIC, TestingNotice.class, packageMsg -> {
                    packageMsg.unsubscribe();
                    promise.complete(packageMsg.getNotice().getValue());

                }, subscribeDone -> {
                    // 3. When the subscribe is done, publish
                    producerDelegate.publish(new TestingNotice("kafka")).future().onFailure(promise::tryFail);
                });
            });

        return promise;
    }

    @Override
    public Promise<CountLikes> countLikes() {
        Promise<CountLikes> promise = Promise.promise();
        likeDao.count().future().map(count -> {
            CountLikes countLikes = new CountLikes();
            countLikes.setCount(count);
            LOGGER.info("CountLikes return the value : {}", count);
            return countLikes;
        }).onComplete(promise::handle);
        return promise;
    }

    @Override
    public Promise<IdLongEntity> addLike(String clientIp) {
        Promise<IdLongEntity> promise = Promise.promise();
        Future<Optional<String>> maxParamFuture = parameterDao.getParameterValue(ParameterConst.LIKE_MAX).future();
        Future<Long> countLikesFuture = likeDao.count().future();

        CompositeFuture.all(maxParamFuture, countLikesFuture).onFailure(promise::fail).onSuccess(compositeResult -> {
            IdLongEntity result = new IdLongEntity();
            Long maxLike = ParameterUtil.getLong(maxParamFuture.result(), 0l);

            if (countLikesFuture.result() > maxLike) {
                LOGGER.warn("AddLike : the maximum of likes is reached");
                promise.complete(result);
                return;
            }

            likeDao.insert(clientIp, new Date()).future().onFailure(promise::fail).onSuccess(insertResult -> {
                result.setId(insertResult);
                LOGGER.info("AddLike successful");
                promise.complete(result);
            });
        });
        return promise;
    }

}
