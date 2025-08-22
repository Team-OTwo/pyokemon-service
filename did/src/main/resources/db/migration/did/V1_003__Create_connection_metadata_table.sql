DROP TABLE IF EXISTS tb_connection_metadata;

CREATE TABLE tb_connection_metadata (
    connection_metadata_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conn_id VARCHAR(255) NOT NULL,
    invi_msg_id VARCHAR(255),
    user_id BIGINT,
    tenant_id BIGINT,
    device_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_conn_id (conn_id),
    INDEX idx_user_id (user_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_tenant (user_id, tenant_id),
    INDEX idx_status (status)
); 