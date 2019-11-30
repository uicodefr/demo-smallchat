package com.uicode.smallchat.smallchatserver;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.uicode.smallchat.smallchatserver.dao.global.LikeDao;
import com.uicode.smallchat.smallchatserver.dao.global.ParameterDao;
import com.uicode.smallchat.smallchatserver.dao.global.impl.LikeDaoImpl;
import com.uicode.smallchat.smallchatserver.dao.global.impl.ParameterDaoImpl;
import com.uicode.smallchat.smallchatserver.messaging.ConsumerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.ProducerDelegate;
import com.uicode.smallchat.smallchatserver.messaging.impl.ConsumerDelegateImpl;
import com.uicode.smallchat.smallchatserver.messaging.impl.ProducerDelegateImpl;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;
import com.uicode.smallchat.smallchatserver.service.GlobalService;
import com.uicode.smallchat.smallchatserver.service.UserService;
import com.uicode.smallchat.smallchatserver.service.impl.ChatStateServiceImpl;
import com.uicode.smallchat.smallchatserver.service.impl.GlobalServiceImpl;
import com.uicode.smallchat.smallchatserver.service.impl.UserServiceImpl;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketMediator;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketServer;
import com.uicode.smallchat.smallchatserver.websocket.impl.WebSocketMediatorImpl;
import com.uicode.smallchat.smallchatserver.websocket.impl.WebSocketServerImpl;

import io.vertx.core.Vertx;

public class MainModule extends AbstractModule {

    private final Vertx vertxInstance;

    public MainModule(Vertx vertxInstance) {
        this.vertxInstance = vertxInstance;
    }

    @Override
    protected void configure() {
        bind(Vertx.class).toInstance(vertxInstance);

        bind(ConsumerDelegate.class).to(ConsumerDelegateImpl.class).in(Scopes.SINGLETON);
        bind(ProducerDelegate.class).to(ProducerDelegateImpl.class).in(Scopes.SINGLETON);

        bind(LikeDao.class).to(LikeDaoImpl.class);
        bind(ParameterDao.class).to(ParameterDaoImpl.class);

        bind(ChatStateService.class).to(ChatStateServiceImpl.class);
        bind(GlobalService.class).to(GlobalServiceImpl.class);
        bind(UserService.class).to(UserServiceImpl.class);

        bind(WebSocketServer.class).to(WebSocketServerImpl.class).in(Scopes.SINGLETON);
        bind(WebSocketMediator.class).to(WebSocketMediatorImpl.class);
    }

}
