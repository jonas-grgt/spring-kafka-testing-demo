package io.jonasg.bank;

import org.testcontainers.kafka.KafkaContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.8.0")
                .withReuse(true)
                .withExposedPorts(9092, 9093);
    }

}