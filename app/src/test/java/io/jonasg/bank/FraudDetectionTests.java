package io.jonasg.bank;

import static io.jonasg.SuspicionReason.UNUSUAL_AMOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import io.jonasg.FraudSuspected;

@BankIntegrationTest
class FraudDetectionTests {

    @Autowired
    ConsumerFactory<String, Object> consumerFactory;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StubIdGenerator stubIdGenerator;

    @Value("${bank.topics.fraud-alert}")
    String fraudAlertTopic;

    private Consumer<String, Object> consumer;

    @AfterEach
    void tearDown() {
        if (this.consumer != null) {
            this.consumer.close();
        }
    }

    @Test
    void fraudSuspectedWhenAmountIsUnusual() throws Exception {
        // given
        String fraudId = UUID.randomUUID().toString();
        stubIdGenerator.setNextId(fraudId);

        // when
        mockMvc.perform(post("/creditcard/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": "10000.001"
                                }
                                """))
                .andExpect(status().isOk());

        // then
        this.consumer = consumerFactory.createConsumer("test-group", "-test-client");
        this.consumer.subscribe(List.of(fraudAlertTopic));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    ConsumerRecords<String, Object> records = KafkaTestUtils
                            .getRecords(this.consumer, Duration.ofMillis(200));

                    assertThat(records)
                            .satisfiesOnlyOnce(record -> {
                                assertThat(record.key()).isEqualTo(fraudId);
                                assertThat(record.value())
                                        .isInstanceOfSatisfying(FraudSuspected.class, c -> {
                                            assertThat(c.getFraudId()).isEqualTo(fraudId);
                                            assertThat(c.getAmount()).isPresent()
                                                    .get()
                                                    .usingComparator(BigDecimal::compareTo)
                                                    .isEqualTo(new BigDecimal("10000.001"));
                                            assertThat(c.getSuspicionReason()).isEqualTo(UNUSUAL_AMOUNT);
                                        });
                            });
                });
    }

    @Test
    void fraudSuspectedWhenAmountIsMissing() throws Exception {
        // given
        String fraudId = UUID.randomUUID().toString();
        stubIdGenerator.setNextId(fraudId);

        // when
        mockMvc.perform(post("/creditcard/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // then
        this.consumer = consumerFactory.createConsumer("test-group", "-test-client");
        this.consumer.subscribe(List.of(fraudAlertTopic));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {

                    ConsumerRecords<String, Object> records = KafkaTestUtils
                            .getRecords(this.consumer, Duration.ofMillis(200));

                    assertThat(records)
                            .satisfiesOnlyOnce(record -> {
                                assertThat(record.key()).isEqualTo(fraudId);
                                assertThat(record.value())
                                        .isInstanceOfSatisfying(FraudSuspected.class, c -> {
                                            assertThat(c.getFraudId()).isEqualTo(fraudId);
                                            assertThat(c.getAmount()).isEmpty();
                                            assertThat(c.getSuspicionReason()).isEqualTo(UNUSUAL_AMOUNT);
                                        });
                            });
                });

    }

}