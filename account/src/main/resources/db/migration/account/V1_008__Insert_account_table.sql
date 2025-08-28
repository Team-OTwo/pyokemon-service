
INSERT IGNORE INTO tb_account (account_id, role, login_id, password, status, created_at, updated_at)
VALUES
    (1, 'TENANT', 'testuser1', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW()),
    (2, 'USER',   'testuser2', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW()),
    (3, 'USER',   'testuser3', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW()),
    (4, 'USER',   'testuser4', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW()),
    (5, 'USER',   'testuser5', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW()),
    (6, 'USER',   'testuser6', '$2a$12$naGBBAp39u5E7YTeLHEejeXh7jRCySIZ2bPvNiS3x/waJ5uSQqo7q', 'ACTIVE', NOW(), NOW());
