package com.bankhub.investment.infrastructure.web.mapper;

import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.web.dto.PortfolioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PortfolioWebMapper {

    @Mapping(source = "id", target = "portfolioId")
    @Mapping(source = "updatedAt", target = "lastUpdate")
    PortfolioResponse toResponse(Portfolio domain);
}
