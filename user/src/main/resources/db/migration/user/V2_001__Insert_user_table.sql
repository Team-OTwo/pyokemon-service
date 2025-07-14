-- 샘플 사용자 데이터 삽입
INSERT INTO tb_user (username, email, password, nickname, status, created_at, updated_at) VALUES
('admin', 'admin@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '관리자', 'ACTIVE', NOW(), NOW()),
('pikachu', 'pikachu@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '피카츄', 'ACTIVE', NOW(), NOW()),
('charizard', 'charizard@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '리자몽', 'ACTIVE', NOW(), NOW()),
('blastoise', 'blastoise@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '거북왕', 'ACTIVE', NOW(), NOW()),
('venusaur', 'venusaur@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '이상해꽃', 'ACTIVE', NOW(), NOW()),
('testuser', 'test@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '테스트유저', 'INACTIVE', NOW(), NOW()); 