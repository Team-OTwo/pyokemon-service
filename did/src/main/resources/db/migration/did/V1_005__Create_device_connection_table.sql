CREATE TABLE tb_device_connection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    connection_id VARCHAR(255) NOT NULL UNIQUE,
    invi_msg_id VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    public_did VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_connection_id (connection_id),
    INDEX idx_invi_msg_id (invi_msg_id),
    INDEX idx_device_id (device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_public_did (public_did),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
); 