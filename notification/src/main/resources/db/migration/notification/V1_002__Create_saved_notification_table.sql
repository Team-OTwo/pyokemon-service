CREATE TABLE tb_saved_notification (
        saved_notification_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
        event_id            BIGINT NOT NULL,
        account_id          BIGINT NOT NULL,
        title               VARCHAR(255) NOT NULL,
        message             TEXT,
        ticket_open_at DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
