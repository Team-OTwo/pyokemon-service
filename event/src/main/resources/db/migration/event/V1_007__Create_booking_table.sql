CREATE TABLE tb_booking (
            booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
            event_schedule_id BIGINT NOT NULL,
            seat_id BIGINT NOT NULL,
            account_id BIGINT NOT NULL,
            payment_id BIGINT,
            status ENUM('PENDING', 'BOOKED', 'CANCELLED') DEFAULT 'PENDING',
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP


)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;