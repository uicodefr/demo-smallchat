package com.uicode.smallchat.smallchatserver;

import org.junit.jupiter.api.BeforeEach;

import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;

public class TestBase extends KafkaTestBase {

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        ConfigUtil.prepareForTest();
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

}
