package com.uicode.smallchat.smallchatserver.dao.global.impl;

import java.util.Optional;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.dao.AbstractDao;
import com.uicode.smallchat.smallchatserver.dao.global.ParameterDao;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

public class ParameterDaoImpl extends AbstractDao implements ParameterDao {

    @Inject
    public ParameterDaoImpl(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Promise<Optional<String>> getParameterValue(String parameterName) {
        Promise<Optional<String>> promise = Promise.promise();
        String sql = "SELECT value FROM global_parameter WHERE name = ?";
        JsonArray params = new JsonArray().add(parameterName);

        getSQLClient().querySingleWithParams(sql, params, queryResult -> {
            if (queryResult.failed()) {
                promise.fail(queryResult.cause());
                return;
            }

            if (queryResult.result().size() <= 0) {
                promise.complete(Optional.empty());
            } else {
                promise.complete(Optional.of(queryResult.result().getString(0)));
            }
        });
        return promise;
    }

}
