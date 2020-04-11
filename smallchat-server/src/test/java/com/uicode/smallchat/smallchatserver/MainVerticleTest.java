package com.uicode.smallchat.smallchatserver;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uicode.smallchat.smallchatserver.model.global.GlobalStatus;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;

public class MainVerticleTest extends TestBase {

    private static final Logger LOGGER = LogManager.getLogger(MainVerticleTest.class);

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext testContext) {
        ConfigUtil.prepareForTest();
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Should start a Web Server on port 8080")
    @Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
    void startServer(Vertx vertx, VertxTestContext testContext) throws Throwable {
        Assertions.assertThat(ConfigUtil.getConfig()).isNotNull();
        Assertions.assertThat(ConfigUtil.getConfig().getKafkaAdmin().get("bootstrap.servers"))
            .isNotNull()
            .contains("localhost");

        WebClient client = WebClient.create(vertx);
        client.get(8080, "localhost", "/global/status").as(BodyCodec.jsonObject()).send(responseResult -> {
            if (responseResult.failed()) {
                LOGGER.error("Get to /global/status failed", responseResult.cause());
            }
            Assertions.assertThat(responseResult.succeeded()).isTrue();
            Assertions.assertThat(responseResult.result().statusCode()).isEqualTo(200);
            GlobalStatus status = responseResult.result().body().mapTo(GlobalStatus.class);
            Assertions.assertThat(status.getUpDate()).isBefore(new Date());
            Assertions.assertThat(status.getMessaging()).isNotEmpty();

            testContext.completeNow();
        });
    }

}
