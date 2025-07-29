CREATE TABLE tb_user_device (
    user_device_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_number VARCHAR(50) NOT NULL,
    fcm_token TEXT,
    os_type ENUM('ANDROID', 'IOS') NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
); 