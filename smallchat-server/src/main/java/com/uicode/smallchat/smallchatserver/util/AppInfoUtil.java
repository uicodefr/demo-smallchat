package com.uicode.smallchat.smallchatserver.util;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class AppInfoUtil {

    private static final String APP_INFO_FILENAME = "app.info.json";

    private AppInfoUtil() {
    }

    public static Promise<JsonObject> getAppInfo(Vertx vertx) {
        Promise<JsonObject> promise = Promise.promise();

        vertx.<Buffer>executeBlocking(bufferPromise -> {
            try {
                InputStream appInfoInputStream = promise.getClass()
                    .getClassLoader()
                    .getResourceAsStream(APP_INFO_FILENAME);
                Buffer buffer = Buffer.buffer(appInfoInputStream.readAllBytes());
                bufferPromise.complete(buffer);
            } catch (IOException exception) {
                bufferPromise.fail(exception);
            }
        }, bufferResult -> {
            if (bufferResult.failed()) {
                promise.fail(bufferResult.cause());
                return;
            }
            promise.complete(bufferResult.result().toJsonObject());
        });

        return promise;
    }

}
