package com.bankhub.account.infrastructure.mapper;

import com.bankhub.account.domain.Card;
import com.bankhub.account.infrastructure.persistence.entity.CardDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toDomain(CardDocument document);

    CardDocument toDocument(Card domain);
}
