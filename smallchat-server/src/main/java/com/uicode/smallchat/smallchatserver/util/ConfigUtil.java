package com.uicode.smallchat.smallchatserver.util;

import java.util.ArrayList;
import java.util.List;

import com.uicode.smallchat.smallchatserver.model.AppConfig;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ConfigUtil {

    private static AppConfig appConfig;

    private static List<JsonObject> jwks;

    private ConfigUtil() {
    }

    public static Promise<Void> initConfig(Vertx vertx) {
        Promise<Void> promise = Promise.promise();
        
        Future<List<JsonObject>> securityConfigFuture = Future.future(promiseSecurityConfig -> {
            ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file")
                    .setConfig(new JsonObject().put("path", "security-conf.json"));
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

            ConfigRetriever confRetriever = ConfigRetriever.create(vertx, options);
            confRetriever.listen(configChangeListener -> {
                JsonArray jwksJsonArray = configChangeListener.getNewConfiguration().getJsonArray("jwks");
                jwks = new ArrayList<>();
                for (int i = 0; i < jwksJsonArray.size(); i++) {
                    jwks.add(jwksJsonArray.getJsonObject(0));
                }

                promiseSecurityConfig.complete(jwks);
            });
        });

        Future<AppConfig> appConfigFuture = Future.future(promiseAppConfig -> {
            ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file")
                    .setConfig(new JsonObject().put("path", "application-conf.json"));
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

            ConfigRetriever confRetriever = ConfigRetriever.create(vertx, options);
            confRetriever.listen(configChangeListener -> {
                appConfig = configChangeListener.getNewConfiguration().mapTo(AppConfig.class);
                promiseAppConfig.tryComplete(appConfig);
            });
        });

        CompositeFuture.join(securityConfigFuture, appConfigFuture)
            .setHandler(compositeHandler -> promise.complete());
        return promise;
    }

    public static AppConfig getConfig() {
        return appConfig;
    }

    public static List<JsonObject> getJwks() {
        return jwks;
    }

}
