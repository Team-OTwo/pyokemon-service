-- Modify connection_id column to allow NULL values in tb_device_connection table
ALTER TABLE tb_device_connection MODIFY COLUMN connection_id VARCHAR(255) UNIQUE;
