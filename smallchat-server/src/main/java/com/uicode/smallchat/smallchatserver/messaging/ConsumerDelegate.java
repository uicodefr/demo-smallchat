package com.uicode.smallchat.smallchatserver.messaging;

import java.util.List;

import com.uicode.smallchat.smallchatserver.model.messagingnotice.AbstractNotice;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

public interface ConsumerDelegate {

    <T extends AbstractNotice> SubscriptionMsg subscribe(String topic, Class<T> type, Handler<PackageMsgNotice<T>> receiveHandler);

    <T extends AbstractNotice> SubscriptionMsg subscribe(String topic, Class<T> type, Handler<PackageMsgNotice<T>> receiveHandler,
            Handler<AsyncResult<Void>> completionHandler);

    void refreshSubscribe(String topic);

    <T extends AbstractNotice> Promise<T> poll(String topic, Class<T> type, long timeout);

    Promise<Void> resendLastMessages(String topic, int messageToResend);

    <T extends AbstractNotice> Promise<List<T>> getLastMessages(String topic, int messageToGet, Class<T> type);

}
