-- 기존 테이블 삭제 및 재생성 (BaseEntity와 일치하도록 수정)
DROP TABLE IF EXISTS tb_payment;

CREATE TABLE tb_payment (
                            id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                            booking_id     BIGINT NOT NULL,
                            event_schedule_id BIGINT NOT NULL,
                            order_id       VARCHAR(255),
                            payment_key    VARCHAR(255),
                            method         VARCHAR(50) NOT NULL,
                            amount         INT NOT NULL,
                            status         ENUM('READY', 'DONE', 'CANCELED', 'FAILED','EXPIRED') NOT NULL DEFAULT 'READY',
                            created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                            updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
