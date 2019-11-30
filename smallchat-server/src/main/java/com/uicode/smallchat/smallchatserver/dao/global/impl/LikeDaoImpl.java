package com.uicode.smallchat.smallchatserver.dao.global.impl;

import java.util.Date;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.dao.AbstractDao;
import com.uicode.smallchat.smallchatserver.dao.global.LikeDao;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

public class LikeDaoImpl extends AbstractDao implements LikeDao {

    @Inject
    public LikeDaoImpl(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Promise<Long> count() {
        Promise<Long> promise = Promise.promise();
        String sql = "SELECT count(1) FROM global_like";

        getSQLClient().querySingle(sql, queryResult -> {
            if (queryResult.failed()) {
                promise.fail(queryResult.cause());
                return;
            }

            if (queryResult.result().size() <= 0) {
                promise.complete(0l);
            } else {
                promise.complete(queryResult.result().getLong(0));
            }
        });
        return promise;
    }

    @Override
    public Promise<Long> insert(String clientIp, Date date) {
        Promise<Long> promise = Promise.promise();
        String sql = "INSERT INTO global_like (client_ip, insert_date) VALUES (?, ?)";
        JsonArray params = new JsonArray().add(clientIp).add(date.getTime());

        getSQLClient().updateWithParams(sql, params, queryResult -> {
            if (queryResult.failed()) {
                promise.fail(queryResult.cause());
                return;
            }

            if (queryResult.result().getKeys().size() <= 0) {
                promise.complete(0l);
            } else {
                promise.complete(queryResult.result().getKeys().getLong(0));
            }
        });
        return promise;
    }

}
