package com.bankhub.notification.application.service;

import com.bankhub.notification.application.port.in.QueryNotificationHistoryUseCase;
import com.bankhub.notification.domain.Notification;
import com.bankhub.notification.infrastructure.persistence.repository.NotificationRepository;
import com.bankhub.notification.infrastructure.web.dto.NotificationDto;
import com.bankhub.notification.infrastructure.web.dto.NotificationHistoryResponse;
import com.bankhub.notification.infrastructure.web.dto.PageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryNotificationHistoryService implements QueryNotificationHistoryUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationHistoryResponse execute(String accountId, int page, int size) {
        log.info("Querying notification history for account: {}, page: {}, size: {}", accountId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByAccountIdOrderBySentAtDesc(accountId, pageable);

        List<NotificationDto> notifications = notificationPage.getContent().stream()
                .map(this::toDto)
                .toList();

        PageMetadata metadata = new PageMetadata(
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.getNumber(),
                notificationPage.getSize()
        );

        log.info("Found {} notifications for account: {}", notifications.size(), accountId);
        return new NotificationHistoryResponse(notifications, metadata);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getReadStatus()
        );
    }
}
