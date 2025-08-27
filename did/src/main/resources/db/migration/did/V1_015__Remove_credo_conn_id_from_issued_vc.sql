-- Remove credo_conn_id column and its index from tb_issued_vc table
ALTER TABLE tb_issued_vc DROP INDEX idx_credo_conn_id;
ALTER TABLE tb_issued_vc DROP COLUMN credo_conn_id;

-- Add credential_id column to tb_issued_vc table
ALTER TABLE tb_issued_vc ADD COLUMN credential_id VARCHAR(255);
ALTER TABLE tb_issued_vc ADD INDEX idx_credential_id (credential_id);
