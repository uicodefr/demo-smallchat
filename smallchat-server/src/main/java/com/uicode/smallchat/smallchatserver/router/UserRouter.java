package com.uicode.smallchat.smallchatserver.router;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.exception.InvalidJWTException;
import com.uicode.smallchat.smallchatserver.model.user.UserLoginData;
import com.uicode.smallchat.smallchatserver.service.UserService;
import com.uicode.smallchat.smallchatserver.util.GeneralConst;

import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class UserRouter {

    private final Vertx vertx;
    private final UserService userService;

    @Inject
    public UserRouter(Vertx vertx, UserService userService) {
        this.vertx = vertx;
        this.userService = userService;
    }

    public void mountSubRouter(Router mainRouter) {
        Router userRouter = Router.router(vertx);
        mainRouter.route().handler(this::authenticate);
        userRouter.post("/login").handler(this::login);
        userRouter.post("/logout").handler(this::logout);
        userRouter.get("/users/me").handler(this::getCurrentUser);
        mainRouter.mountSubRouter("/", userRouter);
    }

    private void authenticate(RoutingContext requestHandler) {
        Cookie jwtCookie = requestHandler.getCookie(GeneralConst.JWTTOKEN_COOKIE);
        if (jwtCookie == null) {
            requestHandler.next();
        } else {
            userService.authenticate(jwtCookie.getValue()).future().onComplete(serviceResult -> {
                if (serviceResult.failed()) {
                    expireCookie(jwtCookie);
                    requestHandler.fail(new InvalidJWTException("Invalid JWT Token", serviceResult.cause()));
                } else {
                    requestHandler.setUser(serviceResult.result());
                    requestHandler.next();
                }
            });
        }
    }

    private void login(RoutingContext requestHandler) {
        String username = requestHandler.request().getFormAttribute("username");
        String password = requestHandler.request().getFormAttribute("password");

        userService.login(username, password).future().onComplete(serviceResult -> {
            if (serviceResult.failed()) {
                requestHandler.fail(serviceResult.cause());
                return;
            }
            if (serviceResult.result().isPresent()) {
                UserLoginData loginData = serviceResult.result().get();
                Cookie jwtCookie = Cookie.cookie(GeneralConst.JWTTOKEN_COOKIE, loginData.getJwtToken());
                jwtCookie.setHttpOnly(true);
                jwtCookie.setMaxAge(43200);
                requestHandler.addCookie(jwtCookie);
                requestHandler.response().end(Json.encode(loginData.getUserPayload()));
            } else {
                requestHandler.response().end();
            }
        });
    }

    private void logout(RoutingContext requestHandler) {
        Cookie jwtCookie = requestHandler.getCookie(GeneralConst.JWTTOKEN_COOKIE);
        if (jwtCookie != null) {
            expireCookie(jwtCookie);
        }
        requestHandler.response().end();
    }

    private void expireCookie(Cookie jwtCookie) {
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
    }

    private void getCurrentUser(RoutingContext requestHandler) {
        if (requestHandler.user() != null) {
            requestHandler.response().end(Json.encode(requestHandler.user().principal()));
        } else {
            requestHandler.response().end();
        }
    }

}
