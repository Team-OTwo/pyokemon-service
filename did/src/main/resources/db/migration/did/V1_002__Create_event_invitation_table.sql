CREATE TABLE tb_event_invitation (
    event_invitation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    invitation_url TEXT NOT NULL,  -- TEXT 타입으로 설정하여 큰 URL 저장 가능
    oob_id VARCHAR(255) NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_event_id (event_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_is_valid (is_valid)
);
