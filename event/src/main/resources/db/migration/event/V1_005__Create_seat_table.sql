CREATE TABLE tb_seat (
    seat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    seat_class_id BIGINT NOT NULL,
    floor BIGINT NOT NULL,
    `row` VARCHAR(10) NOT NULL,
    col VARCHAR(10) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_seat_venue_id ON tb_seat(venue_id);
CREATE INDEX idx_seat_class_id ON tb_seat(seat_class_id);
