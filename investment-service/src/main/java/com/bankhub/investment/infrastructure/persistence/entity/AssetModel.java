package com.bankhub.investment.infrastructure.persistence.entity;

import com.bankhub.investment.domain.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetModel {

    private String ticker;
    private AssetType type;

    @Field(targetType = FieldType.DECIMAL128 )
    private BigDecimal quantity;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal averagePrice;
}
