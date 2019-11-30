package com.uicode.smallchat.smallchatserver.model;

import java.util.Map;

public class AppConfig {

    private Integer confId;

    private Integer httpPort;

    private String sqlitePath;

    private Map<String, String> kafkaProducer;

    private Map<String, String> kafkaConsumer;

    private Map<String, String> kafkaAdmin;

    public Integer getConfId() {
        return confId;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getSqlitePath() {
        return sqlitePath;
    }

    public Map<String, String> getKafkaProducer() {
        return kafkaProducer;
    }

    public Map<String, String> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public Map<String, String> getKafkaAdmin() {
        return kafkaAdmin;
    }

}
