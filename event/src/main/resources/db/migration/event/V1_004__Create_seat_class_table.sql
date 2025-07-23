-- tb_event_schedule 테이블 생성
CREATE TABLE tb_seat_class (
        seat_class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
        class_name VARCHAR(20) NOT NULL,
        priority INT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;