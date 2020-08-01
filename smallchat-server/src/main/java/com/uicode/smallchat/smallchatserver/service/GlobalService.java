package com.uicode.smallchat.smallchatserver.service;

import com.uicode.smallchat.smallchatserver.model.IdLongEntity;
import com.uicode.smallchat.smallchatserver.model.global.CountLikes;
import com.uicode.smallchat.smallchatserver.model.global.GlobalStatus;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public interface GlobalService {

    JsonObject getAppInfo();

    Promise<GlobalStatus> getStatus();

    Promise<CountLikes> countLikes();

    Promise<IdLongEntity> addLike(String clientIp);

}
