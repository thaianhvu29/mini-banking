package com.minibanking.audit.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionAuditConsumer {

    @KafkaListener(
            topics = "transaction-created",
            groupId = "audit-service"
    )
    public void consume(Map<String, Object> event) {

        System.out.println(
                "[AUDIT] Transaction event received: " + event
        );
    }
}
