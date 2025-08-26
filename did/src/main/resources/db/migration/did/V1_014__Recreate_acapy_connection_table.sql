-- Drop and recreate device_connection table with correct schema
DROP TABLE IF EXISTS tb_acapy_connection;

CREATE TABLE tb_acapy_connection (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     connection_id VARCHAR(255) UNIQUE,
     invi_msg_id VARCHAR(255) NOT NULL,
     tenant_id BIGINT NOT NULL,
     user_id BIGINT NOT NULL,
     status VARCHAR(50) NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     INDEX idx_invi_msg_id (invi_msg_id),
     INDEX idx_tenant_id (tenant_id),
     INDEX idx_user_id (user_id),
     INDEX idx_status (status),
     INDEX idx_created_at (created_at)
);
