package io.jonasg.bank;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.kafka.KafkaContainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

public interface KafkaContainerSupport {

    @ServiceConnection
    KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:3.8.0")
            .withReuse(true)
            .withExposedPorts(9092, 9093);

    @BeforeAll
    static void beforeAll() {
        if (!kafkaContainer.isRunning()) {
            kafkaContainer.start();
        }
    }
}
