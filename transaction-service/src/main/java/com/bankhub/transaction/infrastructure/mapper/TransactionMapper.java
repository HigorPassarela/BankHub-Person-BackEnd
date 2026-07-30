package com.bankhub.transaction.infrastructure.mapper;

import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.persistence.entity.TransactionDocument;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionDocument document) {
        if (document == null) {
            return null;
        }

        return Transaction.builder()
                .id(document.getId())
                .sourceAccountId(document.getSourceAccountId())
                .destinationAccountId(document.getDestinationAccountId())
                .amount(document.getAmount())
                .type(document.getType())
                .status(document.getStatus())
                .category(document.getCategory())
                .failureReason(document.getFailureReason())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public TransactionDocument toDocument(Transaction domain) {
        if (domain == null) {
            return null;
        }

        return TransactionDocument.builder()
                .id(domain.id())
                .sourceAccountId(domain.sourceAccountId())
                .destinationAccountId(domain.destinationAccountId())
                .amount(domain.amount())
                .type(domain.type())
                .status(domain.status())
                .category(domain.category())
                .failureReason(domain.failureReason())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}