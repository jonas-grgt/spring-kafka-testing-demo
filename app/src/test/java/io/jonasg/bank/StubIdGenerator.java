package io.jonasg.bank;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class StubIdGenerator implements IdGenerator {

    private String nextId;

    @Override
    public String generateId() {
        var id = nextId != null ? nextId : UUID.randomUUID().toString();
        nextId = null;
        return id;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }
}
