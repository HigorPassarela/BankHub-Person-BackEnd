package com.bankhub.investment.infrastructure.mapper;

import com.bankhub.investment.domain.Asset;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.persistence.entity.AssetModel;
import com.bankhub.investment.infrastructure.persistence.entity.PortfolioDocument;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class PortfolioMapper {

    public Portfolio toDomain(PortfolioDocument document) {
        if (document == null) {
            return null;
        }

        var assets = Optional.ofNullable(document.getAssets())
                .stream()
                .flatMap(Collection::stream)
                .map(this::mapAssetToDomain)
                .toList();

        return Portfolio.builder()
                .id(document.getId())
                .customerId(document.getCustomerId())
                .assets(assets)
                .version(document.getVersion())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public PortfolioDocument toDocument(Portfolio domain) {
        if (domain == null) {
            return null;
        }

        var assetModels = Optional.ofNullable(domain.assets())
                .stream()
                .flatMap(Collection::stream)
                .map(this::mapAssetToModel)
                .toList();

        return PortfolioDocument.builder()
                .id(domain.id())
                .customerId(domain.customerId())
                .assets(assetModels)
                .version(domain.version())
                .updatedAt(domain.updatedAt())
                .build();
    }

    private Asset mapAssetToDomain(AssetModel model) {
        return new Asset(
                model.getTicker(),
                model.getType(),
                model.getQuantity(),
                model.getAveragePrice()
        );
    }

    private AssetModel mapAssetToModel(Asset domain) {
        return AssetModel.builder()
                .ticker(domain.ticker())
                .type(domain.type())
                .quantity(domain.quantity())
                .averagePrice(domain.averagePrice())
                .build();
    }
}
