-- tb_user 테이블 생성
CREATE TABLE tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_user_username ON tb_user(username);
CREATE INDEX idx_user_email ON tb_user(email);
CREATE INDEX idx_user_status ON tb_user(status);
CREATE INDEX idx_user_created_at ON tb_user(created_at);

-- 제약조건 추가
ALTER TABLE tb_user ADD CONSTRAINT chk_user_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')); 