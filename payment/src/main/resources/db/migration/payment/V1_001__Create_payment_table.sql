-- tb_payment 테이블 생성
CREATE TABLE tb_payment (
        payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
        event_schedule_id BIGINT NOT NULL,
        booking_id     BIGINT NOT NULL,
        account_id     BIGINT NOT NULL,
        order_id       VARCHAR(255),
        payment_key    VARCHAR(255),
        method         VARCHAR(50) NOT NULL,
        amount         INT NOT NULL,
        status         ENUM('READY', 'DONE', 'CANCELED', 'FAILED') NOT NULL DEFAULT 'READY',
        created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
