
INSERT IGNORE INTO tb_event
(event_id, account_id, title, age_limit, description, genre, thumbnail_url, status, created_at, updated_at)
VALUES
    (1, 1, '악뮤 콘서트 2025 : [항해]', 12,
     '악동뮤지션의 라이브 밴드 콘서트, 새로운 앨범 수록곡과 히트곡을 만나는 시간',
     '콘서트',
     'https://example.com/thumbnail/akmu.jpg',
     'APPROVED',
     NOW(), NOW()),

    (2, 2, '뮤지컬 레 미제라블', 15,
     '프랑스 대혁명을 배경으로 한 감동적인 대서사시, 2025 한국 공연',
     '뮤지컬',
     'https://example.com/thumbnail/lesmis.jpg',
     'APPROVED',
     NOW(), NOW());
