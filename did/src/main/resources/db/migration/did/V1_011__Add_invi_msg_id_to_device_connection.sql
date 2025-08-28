-- Add invi_msg_id column to device_connection table
ALTER TABLE tb_device_connection 
ADD COLUMN invi_msg_id VARCHAR(255) NOT NULL AFTER user_id,
ADD INDEX idx_invi_msg_id (invi_msg_id);
