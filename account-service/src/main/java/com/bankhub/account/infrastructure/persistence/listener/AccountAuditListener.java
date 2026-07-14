package com.bankhub.account.infrastructure.persistence.listener;

import com.bankhub.account.infrastructure.persistence.entity.AccountAuditLogDocument;
import com.bankhub.account.infrastructure.persistence.entity.AccountDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountAuditListener extends AbstractMongoEventListener<AccountDocument> {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onBeforeSave(BeforeSaveEvent<AccountDocument> event) {
        AccountDocument document = event.getSource();
        String rawData = "{}";

        try {
            rawData = objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar AccountDocument para auditoria. Entity ID: {}", document.getId(), e);
        }

        AccountAuditLogDocument auditLog = AccountAuditLogDocument.builder()
                .accountId(document.getId())
                .action("SAVE_OR_UPDATE")
                .rawData(rawData)
                .timestamp(LocalDateTime.now())
                .build();

        mongoTemplate.insert(auditLog);

        log.info("Log de auditoria gerado com sucesso para a conta ID: {}", document.getId());
    }
}
