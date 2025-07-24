-- email 컬럼을 login_id로 변경
ALTER TABLE tb_tenant CHANGE COLUMN email login_id VARCHAR(255) NOT NULL UNIQUE;

-- 기존 인덱스 삭제하고 새로 생성
DROP INDEX idx_tenant_email ON tb_tenant;
CREATE INDEX idx_tenant_login_id ON tb_tenant(login_id); 