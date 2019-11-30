package com.uicode.smallchat.smallchatserver.dao;

import com.uicode.smallchat.smallchatserver.util.ConfigUtil;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

public abstract class AbstractDao {

    private final Vertx vertx;
    private SQLClient sqlClient;

    public AbstractDao(Vertx vertx) {
        this.vertx = vertx;
    }

    protected SQLClient getSQLClient() {
        if (sqlClient == null) {
            JsonObject jdbcConfig = new JsonObject();
            jdbcConfig.put("url", "jdbc:sqlite:" + ConfigUtil.getConfig().getSqlitePath());
            jdbcConfig.put("driver_class", "org.sqlite.jdbcDriver");

            sqlClient = JDBCClient.createShared(vertx, jdbcConfig);
        }
        return sqlClient;
    }

}
