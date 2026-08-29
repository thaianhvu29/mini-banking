
package com.minibanking.transaction.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEvent(
        Long transactionId,
        Long accountId,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
}
