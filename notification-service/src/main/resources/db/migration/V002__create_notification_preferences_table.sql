CREATE TABLE notification_preferences (
    account_id VARCHAR(255) PRIMARY KEY,
    email_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    push_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
