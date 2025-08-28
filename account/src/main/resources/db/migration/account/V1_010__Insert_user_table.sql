INSERT IGNORE INTO tb_user
(user_id, account_id, name, phone, birth, is_verified, created_at, updated_at)
VALUES
    (1, 2, '김민수', '010-1111-1111', '2000-03-25', TRUE, NOW(), NOW()),
    (2, 3, '박만서', '010-2222-3333', '2000-04-15', TRUE, NOW(), NOW()),
    (3, 4, '이서준', '010-3333-4444', '2000-05-10', TRUE, NOW(), NOW());
