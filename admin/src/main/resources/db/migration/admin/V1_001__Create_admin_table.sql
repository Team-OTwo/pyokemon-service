-- tb_admin 테이블 생성
CREATE TABLE tb_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_admin_username ON tb_admin(username);

-- 기본 관리자 계정 추가
INSERT INTO tb_admin (username, password, role, created_at, updated_at) VALUES
('admin', 'admin123', 'ADMIN', NOW(), NOW());

