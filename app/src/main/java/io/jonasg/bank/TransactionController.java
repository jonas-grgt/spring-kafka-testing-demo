package io.jonasg.bank;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.jonasg.FraudSuspected;
import io.jonasg.SuspicionReason;

@RestController
public class TransactionController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String fraudAlertTopic;

    private final IdGenerator idGenerator;

    public TransactionController(KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bank.topics.fraud-alert}") String fraudAlertTopic,
            IdGenerator idGenerator) {
        this.kafkaTemplate = kafkaTemplate;
        this.fraudAlertTopic = fraudAlertTopic;
        this.idGenerator = idGenerator;
    }

    @PostMapping("/creditcard/transactions")
    public void process(@RequestBody CreditCardTransaction transaction) {

        if (transaction.amount() == null ||
            transaction.amount().compareTo(new BigDecimal("10000")) >= 0) {

            String fraudId = idGenerator.generateId();
            kafkaTemplate.send(fraudAlertTopic, fraudId, FraudSuspected.newBuilder()
                    .setFraudId(fraudId)
                    .setSuspicionReason(SuspicionReason.UNUSUAL_AMOUNT)
                    .setAmount(transaction.amount())
                    .build());

        }
    }
}
