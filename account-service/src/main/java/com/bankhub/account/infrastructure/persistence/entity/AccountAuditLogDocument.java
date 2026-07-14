package com.bankhub.account.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account_audit_logs")
public class AccountAuditLogDocument {

    @Id
    private String id;

    private String accountId;

    private String action;

    private String rawData;

    private LocalDateTime timestamp;
}
