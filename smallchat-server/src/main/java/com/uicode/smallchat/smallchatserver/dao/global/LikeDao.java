package com.uicode.smallchat.smallchatserver.dao.global;

import java.util.Date;

import io.vertx.core.Promise;

public interface LikeDao {

    Promise<Long> count();

    Promise<Long> insert(String clientIp, Date date);

}
