-- tb_payment 테이블 생성
CREATE TABLE tb_payment (
        payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
        booking_id     BIGINT NOT NULL,
        account_id     BIGINT NOT NULL,
        order_id       VARCHAR(255),
        payment_key    VARCHAR(255),
        method         VARCHAR(50) NOT NULL,
        amount         INT NOT NULL,
        status         ENUM('READY', 'IN_PROGRESS', 'WAITING_FOR_DEPOSIT', 'DONE', 'CANCELED', 'PARTIAL_CANCELED', 'ABORTED', 'EXPIRED') NOT NULL DEFAULT 'READY',
        created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payment_booking_id ON tb_payment(booking_id);
CREATE INDEX idx_payment_account_id ON tb_payment(account_id);