CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    account_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT,
    sent_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    read_status BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_notifications_account_id ON notifications(account_id);
