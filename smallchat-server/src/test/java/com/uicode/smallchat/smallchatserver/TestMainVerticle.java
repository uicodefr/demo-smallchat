package com.uicode.smallchat.smallchatserver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Should start a Web Server on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void startServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> testContext.verify(() -> {
            assertTrue(response.statusCode() == 200);
            response.handler(body -> {
                assertTrue(body.toString().isEmpty());
                testContext.completeNow();
            });
        }));
    }

}
