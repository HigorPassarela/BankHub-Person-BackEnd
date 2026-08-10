package com.bankhub.notification.infrastructure.persistence.repository;

import com.bankhub.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a given account ID, ordered by sent timestamp descending.
     *
     * @param accountId Account ID to filter by
     * @param pageable  Pagination parameters
     * @return Page of notifications
     */
    Page<Notification> findByAccountIdOrderBySentAtDesc(String accountId, Pageable pageable);
}
