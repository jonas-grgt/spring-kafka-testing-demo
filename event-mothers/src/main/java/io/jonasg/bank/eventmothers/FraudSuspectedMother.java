package io.jonasg.bank.eventmothers;

import java.math.BigDecimal;
import java.util.UUID;

import io.jonasg.FraudSuspected;
import io.jonasg.SuspicionReason;

@SuppressWarnings("unused")
public class FraudSuspectedMother {

    public static FraudSuspected.Builder aBlogPostCreated() {
        return FraudSuspected.newBuilder()
                .setSuspicionReason(SuspicionReason.UNUSUAL_FREQUENCY)
                .setAmount(new BigDecimal("99.99"))
                .setFraudId(UUID.randomUUID().toString());
    }
}
