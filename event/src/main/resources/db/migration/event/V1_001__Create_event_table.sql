-- tb_event 테이블 생성
CREATE TABLE tb_event (
    event_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    age_limit       BIGINT,
    description     TEXT,
    genre           VARCHAR(100),
    thumbnail_url   VARCHAR(500),
    status          VARCHAR(50) NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_event_tenant_id ON tb_event(tenant_id);
CREATE INDEX idx_event_status ON tb_event(status);
CREATE INDEX idx_event_created_at ON tb_event(created_at);

-- 제약조건 추가
ALTER TABLE tb_event ADD CONSTRAINT chk_event_status
CHECK (status IN ('APPROVED', 'REJECTED', 'PENDING'));