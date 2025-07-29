-- 샘플 사용자 데이터 삽입
INSERT INTO tb_user (name, email, password, phone, birth, created_at, updated_at) VALUES
('pikachu', 'pikachu@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '010-2222-3333', '1995-02-27', NOW(), NOW()),
('charizard', 'charizard@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '010-3333-4444', '1992-07-08', NOW(), NOW()),
('blastoise', 'blastoise@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '010-4444-5555', '1991-03-15', NOW(), NOW()),
('venusaur', 'venusaur@pyokemon.com', '$2a$10$8TuVlyq0JKUNWJYEr3/TaeJ.9k9YNgJnZlUXqCqhOZQKMSJKLGdI2', '010-5555-6666', '1993-05-21', NOW(), NOW()),
('testuser', 'test@pyokemon.com', '12345678', '010-7777-8888', '2000-12-31', NOW(), NOW())