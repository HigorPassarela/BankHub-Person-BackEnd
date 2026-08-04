package com.bankhub.account.infrastructure.persistence.entity;

import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.InvestorProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class AccountDocument {

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String fullName;
    private String phone;
    private String address;

    private AccountNumberModel accountNumber;
    private BalanceModel balance;
    private AccountStatus status;

    private String transactionPinHash;
    private boolean isIdentityVerified;
    private String selfieUrl;

    private InvestorProfile investorProfile;

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
