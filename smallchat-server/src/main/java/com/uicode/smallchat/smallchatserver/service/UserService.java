package com.uicode.smallchat.smallchatserver.service;

import java.util.Optional;

import com.uicode.smallchat.smallchatserver.model.user.UserLoginData;

import io.vertx.core.Promise;
import io.vertx.ext.auth.User;

public interface UserService {

    Promise<Optional<UserLoginData>> login(String username, String password);

    Promise<User> authenticate(String jwtToken);

}
