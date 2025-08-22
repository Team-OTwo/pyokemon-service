CREATE TABLE tb_issued_vc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_exchange_id VARCHAR(255) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    credo_conn_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_credential_exchange_id (credential_exchange_id),
    INDEX idx_booking_id (booking_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_credo_conn_id (credo_conn_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_tenant_booking (tenant_id, booking_id)
); 