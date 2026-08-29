package com.minibanking.transaction.messaging;

import com.minibanking.transaction.entity.Transaction;
import com.minibanking.transaction.event.TransactionCreatedEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String transactionCreatedTopic;

    public TransactionEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.transaction-created}")
            String transactionCreatedTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.transactionCreatedTopic = transactionCreatedTopic;
    }

    public void publishTransactionCreated(Transaction transaction) {

        TransactionCreatedEvent event =
                new TransactionCreatedEvent(
                        transaction.getId(),
                        transaction.getAccountId(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()
                );

        kafkaTemplate.send(
                transactionCreatedTopic,
                String.valueOf(transaction.getId()),
                event
        );
    }
}
