package com.uicode.smallchat.smallchatserver.dao.global;

import java.util.Optional;

import io.vertx.core.Promise;

public interface ParameterDao {

    Promise<Optional<String>> getParameterValue(String parameterName);

}
