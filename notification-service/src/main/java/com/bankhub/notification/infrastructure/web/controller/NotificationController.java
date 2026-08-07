package com.bankhub.notification.infrastructure.web.controller;

import com.bankhub.notification.application.port.in.SendNotificationUseCase;
import com.bankhub.notification.infrastructure.web.dto.NotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Recebida requisição REST para disparo manual de notificação. Conta: {}, Evento: {}", 
                 request.accountId(), request.eventType());
                 
        sendNotificationUseCase.execute(
                request.accountId(),
                request.eventType(),
                request.status(),
                request.agency(),
                request.accountNumber(),
                request.activationToken()
        );
        
        return ResponseEntity.ok().build();
    }
}
