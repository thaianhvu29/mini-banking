package com.minibanking.audit.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionAuditConsumer {

    @KafkaListener(
            topics = "transaction-created",
            groupId = "audit-service"
    )
    public void consume(String event) {
        System.out.println("[AUDIT] Transaction event received: " + event);
    }
}
