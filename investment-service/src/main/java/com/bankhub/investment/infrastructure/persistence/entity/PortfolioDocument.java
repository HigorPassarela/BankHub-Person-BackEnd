package com.bankhub.investment.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfolios")
public class PortfolioDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String customerId;

    private List<AssetModel> assets;

    @Version
    private Long version;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
