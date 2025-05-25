package io.jonasg.bank;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UUIDV4IdGenerator implements IdGenerator {

    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
