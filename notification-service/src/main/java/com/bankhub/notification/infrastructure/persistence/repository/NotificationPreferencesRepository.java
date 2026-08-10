package com.bankhub.notification.infrastructure.persistence.repository;

import com.bankhub.notification.domain.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, String> {

    /**
     * Find notification preferences for a given account ID.
     *
     * @param accountId Account ID to find preferences for
     * @return Optional containing preferences if found
     */
    Optional<NotificationPreferences> findByAccountId(String accountId);
}
