package com.bankhub.investment.domain;

import lombok.Builder;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raiz de Agregação que gerencia a carteira completa de investimentos do cliente.
 */
@Builder
public record Portfolio(
        String id,
        String customerId,
        List<Asset> assets,
        Long version,
        LocalDateTime updatedAt
) {
    // Garante que a lista de ativos nunca seja nula, mesmo numa carteira nova
    public Portfolio {
        if (assets == null) {
            assets = Collections.emptyList();
        } else {
            assets = List.copyOf(assets);
        }
    }

    /**
     * Comportamento de Domínio: Adiciona um novo ativo comprado à carteira.
     * Se o ativo já existir, calcula o novo Preço Médio e soma as cotas.
     */
    public Portfolio addAsset(Asset newAsset) {
        List<Asset> updatedAssets =  new ArrayList<>(this.assets);

        var existingAssetOpt = updatedAssets.stream()
                .filter(a -> a.ticker().equals(newAsset.ticker()))
                .findFirst();

        if (existingAssetOpt.isPresent()) {
            Asset existing = existingAssetOpt.get();
            updatedAssets.remove(existing);

            var totalValueExisting = existing.averagePrice().multiply(existing.quantity());
            var totalValueNew = newAsset.averagePrice().multiply(newAsset.quantity());
            var newQuantity = existing.quantity().add(newAsset.quantity());

            var newAveragePrice = totalValueExisting.add(totalValueNew)
                    .divide(newQuantity, 2, RoundingMode.HALF_UP);

            updatedAssets.add(new Asset(existing.ticker(), existing.type(), newQuantity, newAveragePrice));
        } else {
            updatedAssets.add(newAsset);
        }

        return Portfolio.builder()
                .id(this.id)
                .customerId(this.customerId)
                .assets(updatedAssets)
                .version(this.version)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
