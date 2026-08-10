package com.bankhub.notification.application.service;

import com.bankhub.notification.application.port.in.UpdateReadStatusUseCase;
import com.bankhub.notification.domain.Notification;
import com.bankhub.notification.domain.NotificationNotFoundException;
import com.bankhub.notification.infrastructure.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReadStatusService implements UpdateReadStatusUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        Notification updated = Notification.builder()
                .id(notification.getId())
                .accountId(notification.getAccountId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .readAt(LocalDateTime.now())
                .readStatus(true)
                .build();

        notificationRepository.save(updated);
        log.info("Notification marked as read: {}", notificationId);
    }

    @Override
    @Transactional
    public void markAsUnread(UUID notificationId) {
        log.info("Marking notification as unread: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        Notification updated = Notification.builder()
                .id(notification.getId())
                .accountId(notification.getAccountId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .readAt(null)
                .readStatus(false)
                .build();

        notificationRepository.save(updated);
        log.info("Notification marked as unread: {}", notificationId);
    }

    @Override
    @Transactional
    public int markAllAsRead(String accountId) {
        log.info("Marking all notifications as read for account: {}", accountId);

        List<Notification> unreadNotifications = notificationRepository.findByAccountIdOrderBySentAtDesc(accountId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(n -> !n.getReadStatus())
                .toList();

        LocalDateTime now = LocalDateTime.now();
        List<Notification> updatedNotifications = unreadNotifications.stream()
                .map(n -> Notification.builder()
                        .id(n.getId())
                        .accountId(n.getAccountId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .sentAt(n.getSentAt())
                        .readAt(now)
                        .readStatus(true)
                        .build())
                .toList();

        notificationRepository.saveAll(updatedNotifications);

        log.info("Marked {} notifications as read for account: {}", updatedNotifications.size(), accountId);
        return updatedNotifications.size();
    }
}
