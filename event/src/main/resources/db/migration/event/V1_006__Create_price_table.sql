CREATE TABLE tb_price (
        price_id BIGINT PRIMARY KEY AUTO_INCREMENT,
        event_schedule_id BIGINT NOT NULL,
        seat_class_id BIGINT NOT NULL,
        price INT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE INDEX idx_price_schedule_id ON tb_price(event_schedule_id);
CREATE INDEX idx_price_seat_class_id ON tb_price(seat_class_id);