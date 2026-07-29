package com.bankhub.account.infrastructure.persistence.entity;

import com.bankhub.account.domain.CardType;
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
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cards")
public class CardDocument {

    @Id
    private String id;

    @Indexed
    private String accountId;
    private CardType type;

    @Indexed(unique = true)
    private String cardNumber;

    private String cardholderName;
    private String expirationDate;
    private String cvvHash;
    private String physicalPinHash;

    private boolean isBlocked;
    private boolean nfcEnabled;
    private boolean onlinePurchasesEnabled;
    private boolean internationalUsageEnabled;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal creditLimit;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal availableLimit;

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
