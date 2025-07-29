-- 샘플 테넌트 데이터 삽입
INSERT INTO tb_tenant (login_id, password, corp_name, corp_id, city, street, zipcode, ceo_name, created_at, updated_at) VALUES
('admin@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '파이오케몬', 'PYOKEMON001', '서울시', '강남구 테헤란로 123', '06234', '김대표', NOW(), NOW()),
('nintendo@company.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '닌텐도코리아', 'NINTENDO001', '서울시', '중구 명동길 456', '04567', '이사장', NOW(), NOW()),
('gamefreak@company.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '게임프리크', 'GAMEFREAK01', '부산시', '해운대구 센텀로 789', '48058', '박CEO', NOW(), NOW()),
('test@company.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '테스트컴퍼니', 'TEST001', '대구시', '달서구 테스트로 999', '41900', '최테스트', NOW(), NOW());

-- 샘플 테넌트 DID 데이터 삽입
INSERT INTO tb_tenant_did (tenant_id, did, is_valid, created_at, updated_at) VALUES
(1, 'did:pyokemon:1234567890abcdef', TRUE, NOW(), NOW()),
(1, 'did:pyokemon:abcdef1234567890', FALSE, NOW(), NOW()),
(2, 'did:nintendo:nintendo123456789', TRUE, NOW(), NOW()),
(3, 'did:gamefreak:gamefreak987654', TRUE, NOW(), NOW()),
(4, 'did:test:test000000000000000', TRUE, NOW(), NOW()); 