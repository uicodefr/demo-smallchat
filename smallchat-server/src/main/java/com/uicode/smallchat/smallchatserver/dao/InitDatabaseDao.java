package com.uicode.smallchat.smallchatserver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.SQLConnection;

public class InitDatabaseDao extends AbstractDao {

    private static final Logger LOGGER = LogManager.getLogger(InitDatabaseDao.class);

    private static final String INIT_DATABASE_SQL_PATH = "/init-database.sql";
    private static AtomicBoolean isDatabaseInit = new AtomicBoolean(false);

    private InitDatabaseDao(Vertx vertx) {
        super(vertx);
    }

    public static Promise<Boolean> init(Vertx vertx) {
        InitDatabaseDao dao = new InitDatabaseDao(vertx);
        Promise<Boolean> promise = Promise.promise();

        if (isDatabaseInit.get()) {
            // Already initialized
            promise.complete(false);
            return promise;
        }

        dao.getSQLClient().getConnection(connectResult -> {
            if (connectResult.failed()) {
                LOGGER.error("Connection failed", connectResult.cause());
                promise.fail(connectResult.cause());
                return;
            }

            String initDatabaseSql = null;
            try (InputStream sqlFileStream = dao.getClass().getResourceAsStream(INIT_DATABASE_SQL_PATH)) {
                initDatabaseSql = IOUtils.toString(sqlFileStream, Charset.defaultCharset());
            } catch (IOException exception) {
                LOGGER.error("Read init-database.sql failed", exception);
                promise.fail(exception);
                return;
            }

            Iterator<String> initDbQueryIterator = Arrays.asList(initDatabaseSql.split(";")).iterator();
            executeInitDbQuery(promise, connectResult.result(), initDbQueryIterator);
        });
        return promise;
    }

    private static void executeInitDbQuery(
            Promise<Boolean> promise, SQLConnection connection, Iterator<String> initDbQueryIterator
    ) {
        connection.execute(initDbQueryIterator.next(), queryResult -> {
            if (queryResult.failed()) {
                LOGGER.error("Query failed", queryResult.cause());
                promise.fail(queryResult.cause());
                return;
            }

            if (initDbQueryIterator.hasNext()) {
                executeInitDbQuery(promise, connection, initDbQueryIterator);
            } else {
                LOGGER.info("Database initialized");
                isDatabaseInit.set(true);
                promise.complete(true);
            }
        });
    }

}
