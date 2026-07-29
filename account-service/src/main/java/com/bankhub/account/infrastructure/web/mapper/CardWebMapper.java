package com.bankhub.account.infrastructure.web.mapper;

import com.bankhub.account.domain.Card;
import com.bankhub.account.infrastructure.web.dto.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardWebMapper {

    @Mapping(source = "id", target = "cardId")
    @Mapping(target = "maskedNumber", expression = "java(domain.getMaskedNumber())")
    @Mapping(source = "cvvHash", target = "cvv")
    CardResponse toResponse(Card domain);
}
