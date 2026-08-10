package com.bankhub.notification.application.service;

import com.bankhub.notification.application.port.in.GetNotificationPreferencesUseCase;
import com.bankhub.notification.application.port.in.UpdateNotificationPreferencesUseCase;
import com.bankhub.notification.domain.NotificationPreferences;
import com.bankhub.notification.infrastructure.persistence.repository.NotificationPreferencesRepository;
import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesRequest;
import com.bankhub.notification.infrastructure.web.dto.NotificationPreferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageNotificationPreferencesService implements GetNotificationPreferencesUseCase, UpdateNotificationPreferencesUseCase {

    private final NotificationPreferencesRepository preferencesRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesResponse execute(String accountId) {
        log.info("Retrieving notification preferences for account: {}", accountId);

        NotificationPreferences preferences = preferencesRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    log.info("No preferences found for account: {}, returning defaults", accountId);
                    return NotificationPreferences.builder()
                            .accountId(accountId)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        return toResponse(preferences);
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse execute(String accountId, NotificationPreferencesRequest request) {
        log.info("Updating notification preferences for account: {}", accountId);

        NotificationPreferences preferences = NotificationPreferences.builder()
                .accountId(accountId)
                .emailEnabled(request.emailEnabled())
                .pushEnabled(request.pushEnabled())
                .updatedAt(LocalDateTime.now())
                .build();

        NotificationPreferences saved = preferencesRepository.save(preferences);

        log.info("Notification preferences updated for account: {}", accountId);
        return toResponse(saved);
    }

    private NotificationPreferencesResponse toResponse(NotificationPreferences preferences) {
        return new NotificationPreferencesResponse(
                preferences.getAccountId(),
                preferences.getEmailEnabled(),
                preferences.getPushEnabled(),
                preferences.getUpdatedAt()
        );
    }
}
