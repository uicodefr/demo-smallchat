package com.uicode.smallchat.smallchatserver.service.impl;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.model.user.UserLoginData;
import com.uicode.smallchat.smallchatserver.model.user.UserPayload;
import com.uicode.smallchat.smallchatserver.service.UserService;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;

public class UserServiceImpl implements UserService {
    
    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);

    // XXX Just for the demo
    private static final String PASSWORD_POC = "password";
    private static final String USER_ROLE = "USER";

    private static final JWTOptions JWT_OPTIONS = new JWTOptions().setAlgorithm("ES256").setExpiresInMinutes(60*12);

    private final Vertx vertx;
    private JWTAuth oneJwtProvider;

    @Inject
    public UserServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    private JWTAuth getJwtProvider() {
        if (oneJwtProvider == null) {
            JWTAuthOptions jwtConfig = new JWTAuthOptions().setJwks(ConfigUtil.getJwks());
            jwtConfig.setJWTOptions(JWT_OPTIONS);
            oneJwtProvider = JWTAuth.create(vertx, jwtConfig);
        }
        return oneJwtProvider;
    }

    @Override
    public Promise<Optional<UserLoginData>> login(String username, String password) {
        Promise<Optional<UserLoginData>> promise = Promise.promise();
        // XXX No true authentication : just a password equals to password
        if (StringUtils.isBlank(username) || !PASSWORD_POC.equals(password)) {
            LOGGER.info("Login failed for {}", username);
            promise.complete(Optional.empty());

        } else {
            UserPayload userPayload = new UserPayload();
            userPayload.setUsername(username);
            userPayload.setRoles(Collections.singletonList(USER_ROLE));

            String jwtToken = getJwtProvider().generateToken(JsonObject.mapFrom(userPayload), JWT_OPTIONS);

            UserLoginData userLoginData = new UserLoginData();
            userLoginData.setJwtToken(jwtToken);
            userLoginData.setUserPayload(userPayload);

            LOGGER.info("Login successfull for {}", username);
            promise.complete(Optional.of(userLoginData));
        }
        return promise;
    }

    @Override
    public Promise<User> authenticate(String jwtToken) {
        Promise<User> promise = Promise.promise();
        JsonObject authInfo = new JsonObject().put("jwt", jwtToken).put("options", new JsonObject());

        getJwtProvider().authenticate(authInfo, authResult -> {
            if (authResult.succeeded()) {
                LOGGER.debug("Authenticate successfull");
                promise.complete(authResult.result());
            } else {
                LOGGER.debug("Authenticate failed");
                promise.fail(authResult.cause());
            }
        });
        return promise;
    }

}
