package com.uicode.smallchat.smallchatserver.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger(ConfigUtil.class);

    private static final String PROFILE_ENV = "APP_PROFILE";
    private static final String SECURITY_ENV = "APP_SECURITY";
    private static final String KAFKA_BOOTSTRAP_SERVER_ENV = "APP_KAFKA_BOOTSTRAP_SERVER";
    private static final String PROFILE_PROD_VALUE = "prod";
    private static final String KAFKA_BOOTSTRAP_CONFIG_KEY = "bootstrap.servers";

    private static AppConfig appConfig;

    private static List<JsonObject> jwks;

    private ConfigUtil() {
    }

    public static Promise<Void> initConfig(Vertx vertx) {
        Promise<Void> promise = Promise.promise();

        ConfigStoreOptions envStore = new ConfigStoreOptions().setType("env");
        ConfigRetrieverOptions envOptions = new ConfigRetrieverOptions().addStore(envStore);
        ConfigRetriever envConfRetriever = ConfigRetriever.create(vertx, envOptions);
        envConfRetriever.getConfig(envConfigResult -> {
            if (envConfigResult.failed()) {
                promise.fail(envConfigResult.cause());
                return;
            }

            Future<List<JsonObject>> securityConfigFuture = getSecurityConfig(vertx, envConfigResult.result());
            Future<AppConfig> appConfigFuture = getAppConfig(vertx, envConfigResult.result());

            CompositeFuture.all(securityConfigFuture, appConfigFuture).<Void>mapEmpty().onComplete(promise::handle);
        });

        return promise;
    }

    private static Future<List<JsonObject>> getSecurityConfig(Vertx vertx, JsonObject envObject) {
        return Future.future(securityConfigPromise -> {
            String profile = envObject.getString(PROFILE_ENV);
            if (PROFILE_PROD_VALUE.equalsIgnoreCase(profile)) {
                // In Prod
                LOGGER.info("Get Security Config for prod");
                JsonObject securityObject = envObject.getJsonObject(SECURITY_ENV);
                JsonArray jwksJsonArray = securityObject.getJsonArray("jwks");
                jwks = new ArrayList<>();
                for (int i = 0; i < jwksJsonArray.size(); i++) {
                    jwks.add(jwksJsonArray.getJsonObject(i));
                }
                securityConfigPromise.complete(jwks);

            } else {
                // In Dev
                LOGGER.info("Get Security Config for dev");
                ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file")
                        .setConfig(new JsonObject().put("path", "security-conf.json"));
                ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

                ConfigRetriever confRetriever = ConfigRetriever.create(vertx, options);
                confRetriever.getConfig(configResult -> {
                    if (configResult.failed()) {
                        securityConfigPromise.fail(configResult.cause());
                        return;
                    }

                    JsonArray jwksJsonArray = configResult.result().getJsonArray("jwks");
                    jwks = new ArrayList<>();
                    for (int i = 0; i < jwksJsonArray.size(); i++) {
                        jwks.add(jwksJsonArray.getJsonObject(i));
                    }
                    securityConfigPromise.complete(jwks);
                });
            }
        });
    }

    private static Future<AppConfig> getAppConfig(Vertx vertx, JsonObject envObject) {
        return Future.future(appConfigPromise -> {
            String profile = envObject.getString(PROFILE_ENV);
            String file;
            if (PROFILE_PROD_VALUE.equalsIgnoreCase(profile)) {
                // Other conf file in prod
                file = "application-conf-prod.json";
                LOGGER.info("Get App Config for prod");
            } else {
                file = "application-conf.json";
                LOGGER.info("Get App Config for dev");
            }

            ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file")
                    .setConfig(new JsonObject().put("path", file));
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);

            ConfigRetriever confRetriever = ConfigRetriever.create(vertx, options);
            confRetriever.getConfig(configResult -> {
                if (configResult.failed()) {
                    appConfigPromise.fail(configResult.cause());
                    return;
                }

                appConfig = configResult.result().mapTo(AppConfig.class);
                if (envObject.containsKey(KAFKA_BOOTSTRAP_SERVER_ENV)) {
                    // We can change the kafka bootstrap server with an environment variable
                    String kafkaBootstrapServer = envObject.getString(KAFKA_BOOTSTRAP_SERVER_ENV);
                    appConfig.getKafkaConsumer().put(KAFKA_BOOTSTRAP_CONFIG_KEY, kafkaBootstrapServer);
                    appConfig.getKafkaProducer().put(KAFKA_BOOTSTRAP_CONFIG_KEY, kafkaBootstrapServer);
                    appConfig.getKafkaAdmin().put(KAFKA_BOOTSTRAP_CONFIG_KEY, kafkaBootstrapServer);
                    LOGGER.info("Change kafka bootstrap server with {} ", kafkaBootstrapServer);
                }

                appConfigPromise.complete(appConfig);
            });
        });
    }

    public static AppConfig getConfig() {
        return appConfig;
    }

    public static List<JsonObject> getJwks() {
        return jwks;
    }

}
