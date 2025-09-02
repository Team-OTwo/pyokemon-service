-- Admin 관련 테이블들을 모두 DROP
DROP TABLE IF EXISTS tb_admin;

-- 기존 Admin 계정 데이터 제거
DELETE FROM tb_account WHERE role = 'ADMIN';

-- 기본 Admin 계정: admin@pyokemon.com / admin123!!
INSERT INTO tb_account (role, login_id, password, status, created_at, updated_at)
VALUES ('ADMIN', 'admin@pyokemon.com', '$2a$10$h.Dtflg21CuryrW3lLEHXuECn3OxBTKnD/xs9U0ybWZNp8SYcwILm', 'ACTIVE', NOW(), NOW());

-- Admin 테이블 재생성
CREATE TABLE tb_admin (
                          admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          account_id BIGINT NOT NULL,
                          name VARCHAR(20) NOT NULL,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Admin 계정의 상세 정보 생성
INSERT INTO tb_admin (account_id, name, created_at, updated_at)
SELECT id, 'admin', NOW(), NOW() FROM tb_account WHERE login_id = 'admin@pyokemon.com';

