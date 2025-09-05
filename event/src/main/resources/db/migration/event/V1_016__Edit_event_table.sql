DROP TABLE IF EXISTS tb_event;
-- tb_event 테이블 생성
CREATE TABLE tb_event (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id      BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    age_limit       BIGINT,
    description     TEXT,
    genre           VARCHAR(100),
    thumbnail_url   VARCHAR(500),
    status          ENUM('APPROVED', 'REJECTED', 'PENDING', 'CANCELED') DEFAULT 'PENDING',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
