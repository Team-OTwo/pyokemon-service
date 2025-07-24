-- tb_admin 테이블 생성
CREATE TABLE tb_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    admin_id VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_admin_admin_id ON tb_admin(admin_id);

-- 기본 관리자 계정 추가
INSERT INTO tb_admin (username, password, admin_id, created_at, updated_at) VALUES
('admin', 'admin123', 'admin', NOW(), NOW());

