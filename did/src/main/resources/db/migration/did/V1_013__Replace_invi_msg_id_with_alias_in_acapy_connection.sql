-- V1.013: Replace invi_msg_id with alias in AcaPyConnection table
-- AcaPyConnection 테이블의 invi_msg_id 컬럼을 alias로 변경

-- 1. alias 컬럼 추가
ALTER TABLE tb_acapy_connection ADD COLUMN alias VARCHAR(500) COMMENT 'Tracking ID for invitation identification';

-- 2. 기존 데이터에 대한 alias 설정 (기존 invi_msg_id가 있다면 임시로 설정)
UPDATE tb_acapy_connection 
SET alias = CONCAT('Legacy AcaPy Connection [id:', id, ']') 
WHERE alias IS NULL;

-- 3. invi_msg_id 컬럼 제거
ALTER TABLE tb_acapy_connection DROP COLUMN invi_msg_id;

-- 4. alias 컬럼에 인덱스 추가 (빠른 조회를 위해)
CREATE INDEX idx_acapy_connection_alias ON tb_acapy_connection(alias);

-- 5. alias 컬럼을 NOT NULL로 설정
ALTER TABLE tb_acapy_connection MODIFY COLUMN alias VARCHAR(500) NOT NULL COMMENT 'Tracking ID for invitation identification';
