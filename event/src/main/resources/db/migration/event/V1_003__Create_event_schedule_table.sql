-- tb_event_schedule 테이블 생성
CREATE TABLE tb_event_schedule (
    event_schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    ticket_open_at DATETIME,
    event_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_schedule_event_id FOREIGN KEY (event_id) REFERENCES tb_event(event_id),
    CONSTRAINT fk_event_schedule_venue_id FOREIGN KEY (venue_id) REFERENCES tb_venue(venue_id),

    INDEX idx_event_schedule_event_id (event_id),
    INDEX idx_event_schedule_venue_id (venue_id),
    INDEX idx_event_schedule_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;