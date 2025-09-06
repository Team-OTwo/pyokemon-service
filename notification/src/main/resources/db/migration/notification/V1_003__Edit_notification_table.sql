DROP TABLE IF EXISTS tb_notification;
CREATE TABLE tb_notification (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id          BIGINT NOT NULL,
    title               VARCHAR(255) NOT NULL,
    message             TEXT,
    is_checked          BOOLEAN DEFAULT FALSE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)