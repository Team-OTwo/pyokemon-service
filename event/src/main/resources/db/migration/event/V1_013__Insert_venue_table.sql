
INSERT IGNORE INTO tb_venue
(venue_id, venue_name, city, street, zipcode, created_at, updated_at)
VALUES
    (1, '서울월드컵경기장', '서울특별시 마포구', '성산로 515', '03906', NOW(), NOW()),
    (2, '예술의전당 오페라극장', '서울특별시 서초구', '남부순환로 2406', '06757', NOW(), NOW());
