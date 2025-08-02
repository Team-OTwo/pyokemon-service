CREATE TABLE tb_booking (
            booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
            event_schedule_id BIGINT NOT NULL,
            seat_id BIGINT NOT NULL,
            account_id BIGINT NOT NULL,
            payment_id BIGINT,
            status ENUM('PENDING', 'BOOKED', 'CANCELLED') DEFAULT 'PENDING',
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            CONSTRAINT fk_booking_schedule FOREIGN KEY (event_schedule_id) REFERENCES tb_event_schedule(event_schedule_id),
            CONSTRAINT fk_booking_seat FOREIGN KEY (seat_id) REFERENCES tb_seat(seat_id)


)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_booking_schedule_id ON tb_booking(event_schedule_id);
CREATE INDEX idx_booking_seat_id ON tb_booking(seat_id);
CREATE INDEX idx_booking_user_id ON tb_booking(account_id);