package com.minibanking.notification.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionNotificationConsumer {

    @KafkaListener(
            topics = "transaction-created",
            groupId = "notification-service"
    )
    public void consume(String event) {
        System.out.println("[NOTIFICATION] Transaction notification processed: " + event);
    }
}
