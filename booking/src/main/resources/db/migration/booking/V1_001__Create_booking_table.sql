CREATE TABLE tb_booking (
    booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    payment_id BIGINT,
    status ENUM('PENDING', 'BOOKED', 'CANCELLED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_event_schedule_id (event_schedule_id),
    INDEX idx_account_id (account_id),
    INDEX idx_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
