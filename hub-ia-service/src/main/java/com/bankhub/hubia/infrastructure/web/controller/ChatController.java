package com.bankhub.hubia.infrastructure.web.controller;

import com.bankhub.hubia.application.service.ChatOrchestratorService;
import com.bankhub.hubia.infrastructure.web.dto.ChatRequest;
import com.bankhub.hubia.infrastructure.web.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Assistente Virtual Bank-Hub integrado com LLM (LangChain4j)")
public class ChatController {

    private final ChatOrchestratorService chatOrchestratorService;

    @PostMapping("/{accountId}")
    @Operation(summary = "Envia uma mensagem para o Assistente de Inteligência Artificial do banco.")
    public ResponseEntity<ChatResponse> chat(
            @Parameter(description = "ID da conta que está interagindo no chat")
            @PathVariable String accountId,

            @Parameter(hidden = true, description = "Injetado via API Gateway")
            @RequestHeader("X-User-Id") String customerId,

            @Valid @RequestBody ChatRequest request) {

        log.info("Recebida requisição de chat REST. Conta: {}, Titular: {}", accountId, customerId);

        String aiReply = chatOrchestratorService.processChat(accountId, customerId, request.message());

        ChatResponse response = ChatResponse.builder()
                .reply(aiReply)
                .build();

        return ResponseEntity.ok(response);
    }
}
