package com.bankhub.notification.infrastructure.web.controller;

import com.bankhub.notification.application.port.in.GetNotificationPreferencesUseCase;
import com.bankhub.notification.application.port.in.QueryNotificationHistoryUseCase;
import com.bankhub.notification.application.port.in.SendNotificationUseCase;
import com.bankhub.notification.application.port.in.UpdateNotificationPreferencesUseCase;
import com.bankhub.notification.application.port.in.UpdateReadStatusUseCase;
import com.bankhub.notification.infrastructure.web.dto.NotificationDto;
import com.bankhub.notification.infrastructure.web.dto.NotificationHistoryResponse;
import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesRequest;
import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesResponse;
import com.bankhub.notification.infrastructure.web.dto.NotificationRequest;
import com.bankhub.notification.infrastructure.web.dto.UpdateReadStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints for querying history, managing read status, and configuring preferences")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final QueryNotificationHistoryUseCase queryNotificationHistoryUseCase;
    private final UpdateReadStatusUseCase updateReadStatusUseCase;
    private final GetNotificationPreferencesUseCase getNotificationPreferencesUseCase;
    private final UpdateNotificationPreferencesUseCase updateNotificationPreferencesUseCase;

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

    @Operation(
        summary = "Query notification history",
        description = "Retrieves paginated notification history for a specific account. Returns notifications ordered by sent date (most recent first)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved notification history",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = NotificationHistoryResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid parameters",
            content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Invalid page parameters",
                    value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Page number and size must be non-negative\",\"instance\":\"/api/v1/notifications\"}"
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<NotificationHistoryResponse> queryNotificationHistory(
            @Parameter(description = "Account ID to query notifications for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String accountId,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (number of notifications per page)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Consultando histórico de notificações. Conta: {}, Página: {}, Tamanho: {}", accountId, page, size);
        NotificationHistoryResponse response = queryNotificationHistoryUseCase.execute(accountId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update notification read status",
        description = "Marks a specific notification as read or unread. Updates the read timestamp when marking as read."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated notification read status"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid request body",
            content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Missing read status",
                    value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Read status is required\",\"instance\":\"/api/v1/notifications/{id}/read\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Notification not found",
                    value = "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"Notification not found with ID: 550e8400-e29b-41d4-a716-446655440000\",\"instance\":\"/api/v1/notifications/550e8400-e29b-41d4-a716-446655440000/read\"}"
                )
            )
        )
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> updateReadStatus(
            @Parameter(description = "Notification ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Read status update request",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateReadStatusRequest.class)
                )
            )
            @Valid @RequestBody UpdateReadStatusRequest request
    ) {
        log.info("Atualizando status de leitura da notificação: {}, Lido: {}", id, request.readStatus());

        if (request.readStatus()) {
            updateReadStatusUseCase.markAsRead(id);
        } else {
            updateReadStatusUseCase.markAsUnread(id);
        }

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all unread notifications for a specific account as read. Updates read timestamps for all affected notifications."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully marked all notifications as read"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid account ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Invalid account ID",
                    value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Account ID is required\",\"instance\":\"/api/v1/notifications/mark-all-read\"}"
                )
            )
        )
    })
    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "Account ID to mark all notifications as read", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String accountId
    ) {
        log.info("Marcando todas as notificações como lidas. Conta: {}", accountId);
        int updatedCount = updateReadStatusUseCase.markAllAsRead(accountId);
        log.info("Marcadas {} notificações como lidas para a conta: {}", updatedCount, accountId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get notification preferences",
        description = "Retrieves notification preferences for a specific account. Returns default preferences (all channels enabled) if none exist."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved notification preferences",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = NotificationPreferencesResponse.class)
            )
        )
    })
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @Parameter(description = "Account ID to retrieve preferences for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String accountId
    ) {
        log.info("Consultando preferências de notificação. Conta: {}", accountId);
        NotificationPreferencesResponse response = getNotificationPreferencesUseCase.execute(accountId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update notification preferences",
        description = "Updates notification preferences for a specific account. Allows enabling/disabling email and push notification channels."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated notification preferences",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = NotificationPreferencesResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation errors",
            content = @Content(
                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Validation error",
                    value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"Email enabled and push enabled fields are required\",\"instance\":\"/api/v1/notifications/preferences\"}"
                )
            )
        )
    })
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @Parameter(description = "Account ID to update preferences for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String accountId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Notification preferences update request",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = NotificationPreferencesRequest.class)
                )
            )
            @Valid @RequestBody NotificationPreferencesRequest request
    ) {
        log.info("Atualizando preferências de notificação. Conta: {}, Email: {}, Push: {}",
                 accountId, request.emailEnabled(), request.pushEnabled());
        NotificationPreferencesResponse response = updateNotificationPreferencesUseCase.execute(accountId, request);
        return ResponseEntity.ok(response);
    }
}
