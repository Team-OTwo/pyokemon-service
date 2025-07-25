-- tb_tenant 테이블 생성
CREATE TABLE tb_tenant (
    tenant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    corp_name VARCHAR(20) NOT NULL,
    corp_id VARCHAR(20) NOT NULL UNIQUE,
    city VARCHAR(20),
    street VARCHAR(20),
    zipcode VARCHAR(20),
    ceo VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_tenant_email ON tb_tenant(email);
CREATE INDEX idx_tenant_corp_id ON tb_tenant(corp_id);
CREATE INDEX idx_tenant_corp_name ON tb_tenant(corp_name);
CREATE INDEX idx_tenant_created_at ON tb_tenant(created_at);
