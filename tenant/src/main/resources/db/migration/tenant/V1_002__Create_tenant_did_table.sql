-- tb_tenant_did 테이블 생성
CREATE TABLE tb_tenant_did (
    tenant_did_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    did TEXT NOT NULL,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 외래키 제약조건
    CONSTRAINT fk_tenant_did_tenant_id 
        FOREIGN KEY (tenant_id) REFERENCES tb_tenant(tenant_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_tenant_did_tenant_id ON tb_tenant_did(tenant_id);
CREATE INDEX idx_tenant_did_is_valid ON tb_tenant_did(is_valid);
CREATE INDEX idx_tenant_did_created_at ON tb_tenant_did(created_at);

-- DID 중복 방지를 위한 유니크 인덱스
CREATE UNIQUE INDEX idx_tenant_did_unique ON tb_tenant_did(did(255)); 