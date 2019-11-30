package com.uicode.smallchat.smallchatserver.messaging;

import com.uicode.smallchat.smallchatserver.model.message.AbstractMessage;

import io.vertx.core.Handler;
import io.vertx.core.Promise;

public interface ConsumerDelegate {

    <T extends AbstractMessage> SubscriptionMsg subscribe(String topic, Class<T> type, Handler<PackageMsg<T>> handler);

    <T extends AbstractMessage> Promise<T> poll(String topic, Class<T> type, long timeout);

    Promise<Void> resendLastMessage(String topic, int messageToResend);

}
