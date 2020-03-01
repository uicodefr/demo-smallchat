package com.uicode.smallchat.smallchatserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.uicode.smallchat.smallchatserver.dao.InitDatabaseDao;
import com.uicode.smallchat.smallchatserver.router.MainRouter;
import com.uicode.smallchat.smallchatserver.util.ConfigUtil;
import com.uicode.smallchat.smallchatserver.websocket.WebSocketServer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(MainVerticle.class);

    public static void main(String[] args) {

        VertxOptions vertxOptions = new VertxOptions();
        // Uncomment the line below in debug
        // vertxOptions.setBlockedThreadCheckInterval(999888777666L);
        Vertx myVertx = Vertx.vertx(vertxOptions);
        myVertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Injector injector = Guice.createInjector(new MainModule(vertx));

        LOGGER.info("Application starting");

        ConfigUtil.initConfig(vertx).future().compose(mapper -> InitDatabaseDao.init(vertx).future()).setHandler(initDbResult -> {
            if (initDbResult.failed()) {
                startPromise.fail(initDbResult.cause());
                return;
            }
            
            MainRouter mainRouter = injector.getInstance(MainRouter.class);
            WebSocketServer webSocketHandler = injector.getInstance(WebSocketServer.class);
            Integer httpPort = ConfigUtil.getConfig().getHttpPort();

            // Http Server
            mainRouter.mountRouter();
            HttpServer httpServer = vertx.createHttpServer().requestHandler(mainRouter.getRouter());
            httpServer.webSocketHandler(webSocketHandler::handleWebSocket);
            httpServer.listen(httpPort, http -> {
                if (http.succeeded()) {
                    startPromise.complete();
                    LOGGER.info(String.format("HTTP server started on : %d", httpPort));
                } else {
                    startPromise.fail(http.cause());
                    LOGGER.error("HTTP server error", http.cause());
                }
            });

        });
    }

}
