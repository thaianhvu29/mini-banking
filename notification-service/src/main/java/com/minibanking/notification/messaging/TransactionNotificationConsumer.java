package com.minibanking.notification.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionNotificationConsumer {

    @KafkaListener(
            topics = "transaction-created",
            groupId = "notification-service"
    )
    public void consume(Map<String, Object> event) {

        System.out.println(
                "[NOTIFICATION] Transaction notification processed: " + event
        );
    }
}
