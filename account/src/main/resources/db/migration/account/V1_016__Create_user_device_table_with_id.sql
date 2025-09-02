CREATE TABLE tb_user_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(100),
    device_type ENUM('MOBILE', 'TABLET', 'DESKTOP') DEFAULT 'MOBILE',
    is_valid BOOLEAN DEFAULT TRUE,
    is_login BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    UNIQUE KEY unique_user_device (user_id, device_id)
);
