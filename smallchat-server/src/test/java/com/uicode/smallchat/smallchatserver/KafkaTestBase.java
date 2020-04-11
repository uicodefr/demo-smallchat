package com.uicode.smallchat.smallchatserver;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)
public class KafkaTestBase {

    private static File dataDir;
    protected static KafkaCluster kafkaCluster;

    protected static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(2181, 9092);
        return kafkaCluster;
    }

    @BeforeAll
    public static void setUp() throws IOException {
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true).addBrokers(1).startup();
    }

    @AfterAll
    public static void tearDown() {
        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }
}
