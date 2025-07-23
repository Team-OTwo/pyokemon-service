-- tb_venue 테이블 생성
CREATE TABLE tb_venue (
    venue_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(30) NOT NULL,
    city VARCHAR(20),
    street VARCHAR(20),
    zipcode VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_venue_name (venue_name),
    INDEX idx_venue_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;