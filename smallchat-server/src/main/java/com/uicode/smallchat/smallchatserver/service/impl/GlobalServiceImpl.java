package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.dao.global.LikeDao;
import com.uicode.smallchat.smallchatserver.dao.global.ParameterDao;
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

    private static final String VERSION = "0.1.3-SNAPSHOT";
    private static final Date UPDATE = new Date();

    private final ParameterDao parameterDao;
    private final LikeDao likeDao;

    private final ProducerDelegate producerDelegate;
    private final ConsumerDelegate consumerDelegate;

    @Inject
    public GlobalServiceImpl(
        ParameterDao parameterDao,
        LikeDao likeDao,
        ProducerDelegate producerDelegate,
        ConsumerDelegate consumerDelegate
    ) {
        this.parameterDao = parameterDao;
        this.likeDao = likeDao;

        this.producerDelegate = producerDelegate;
        this.consumerDelegate = consumerDelegate;
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
        CompositeFuture.join(testMessagingFuture, testDaoFuture).setHandler(compositeResult -> {
            if (testMessagingFuture.failed()) {
                promise.fail(testMessagingFuture.cause());
                return;
            }
            if (testDaoFuture.failed()) {
                promise.fail(testDaoFuture.cause());
                return;
            }

            status.setMessaging(testMessagingFuture.result());
            testDaoFuture.result().ifPresent(status::setStatus);
            promise.complete(status);
        });
        LOGGER.info("Global Status asked");
        return promise;
    }

    private Promise<String> testMessaging() {
        Promise<String> promise = Promise.promise();
        consumerDelegate.subscribe(TestingNotice.TOPIC, TestingNotice.class, packageMsg -> {
            packageMsg.unsubscribe();
            promise.complete(packageMsg.getNotice().getValue());
        });

        producerDelegate.publish(new TestingNotice("kafka")).future().setHandler(recordResult -> {
            if (recordResult.failed()) {
                promise.tryFail(recordResult.cause());
            }
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
        }).setHandler(promise::handle);
        return promise;
    }

    @Override
    public Promise<IdLongEntity> addLike(String clientIp) {
        Promise<IdLongEntity> promise = Promise.promise();
        Future<Optional<String>> maxParamFuture = parameterDao.getParameterValue(ParameterConst.LIKE_MAX).future();
        Future<Long> countLikesFuture = likeDao.count().future();

        CompositeFuture.join(maxParamFuture, countLikesFuture).setHandler(compositeResult -> {
            if (maxParamFuture.failed()) {
                promise.fail(maxParamFuture.cause());
                return;
            }
            if (countLikesFuture.failed()) {
                promise.fail(countLikesFuture.cause());
                return;
            }

            IdLongEntity result = new IdLongEntity();
            Long maxLike = ParameterUtil.getLong(maxParamFuture.result(), 0l);

            if (countLikesFuture.result() > maxLike) {
                LOGGER.warn("AddLike : the maximum of likes is reached");
                promise.complete(result);
                return;
            }

            likeDao.insert(clientIp, new Date()).future().setHandler(insertResult -> {
                if (insertResult.failed()) {
                    promise.fail(insertResult.cause());
                    return;
                }

                result.setId(insertResult.result());
                LOGGER.info("AddLike successful");
                promise.complete(result);
            });
        });
        return promise;
    }

}
