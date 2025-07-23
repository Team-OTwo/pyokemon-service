-- 공연장 정보를 tb_venue 테이블에 삽입
INSERT INTO tb_venue (venue_id, venue_name, city, street, zipcode) VALUES
(1, '잠실종합운동장', '서울특별시', '올림픽로 25', '05508');

-- 좌석 등급(VIP, R, A, B) 정보를 tb_seat_class 테이블에 삽입
INSERT INTO tb_seat_class (seat_class_id, class_name, priority) VALUES
(1, 'VIP', 1),
(2, 'R', 2),
(3, 'A', 3),
(4, 'B', 4);

-- 재귀 CTE를 사용하여 A열부터 L열까지 각 열에 10석씩, 총 120개의 VIP 좌석을 tb_seat 테이블에 삽입
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE Seats(n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1 FROM Seats WHERE n < 119
)
SELECT
    1,
    1,
    1,
    CHAR(ASCII('A') + (n DIV 10)),
    (n MOD 10) + 1
FROM Seats;

-- 두 가지 이벤트('피카에몬 라이브 콘서트', '별의 꿈 뮤지컬') 정보를 tb_event 테이블에 삽입
INSERT INTO tb_event (event_id, tenant_id, title, age_limit, description, genre, thumbnail_url, status) VALUES
(1, 1, '피카에몬 라이브 콘서트', 7, '모두가 기다려온 피카에몬의 환상적인 라이브 콘서트!', '콘서트', 'http://example.com/thumbnail1.jpg', 'APPROVED'),
(2, 1, '별의 꿈 뮤지컬', 0, '어린이들을 위한 따뜻한 감동의 뮤지컬', '뮤지컬', 'http://example.com/thumbnail2.jpg', 'APPROVED');

-- 두 이벤트에 대한 공연 일정 정보를 tb_event_schedule 테이블에 삽입
INSERT INTO tb_event_schedule (event_schedule_id, event_id, venue_id, ticket_open_at, event_date) VALUES
(1, 1, 1, '2025-06-23 10:00:00', '2025-07-30 19:00:00'),
(2, 2, 1, '2025-06-25 10:00:00', '2025-08-05 14:00:00');

-- 첫 번째 공연 일정(event_schedule_id=1)에 대한 좌석 등급별 가격 정보를 tb_price 테이블에 삽입
INSERT INTO tb_price (event_schedule_id, seat_class_id, price) VALUES
(1, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'VIP'), 198000),
(1, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'R'), 178000),
(1, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'A'), 148000),-

-- 두 번째 공연 일정(event_schedule_id=2)에 대한 좌석 등급별 가격 정보를 tb_price 테이블에 삽입
INSERT INTO tb_price (event_schedule_id, seat_class_id, price) VALUES
(2, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'VIP'), 198000),
(2, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'R'), 178000),
(2, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'A'), 148000),
(2, (SELECT seat_class_id FROM tb_seat_class WHERE class_name = 'B'), 128000);

-- tb_booking 테이블
INSERT INTO tb_booking (booking_id, event_schedule_id, user_id, payment_id, status, seat_id, created_at, updated_at)
VALUES
    (1001, 1, 100, 1000, 'BOOKED', (SELECT seat_id FROM tb_seat WHERE venue_id = 1 AND `row` = 'A' AND col = '1'), NOW(), NOW()),
    (1002, 1, 101, 1001, 'BOOKED', (SELECT seat_id FROM tb_seat WHERE venue_id = 1 AND `row` = 'A' AND col = '2'), NOW(), NOW()),
    (1003, 1, 103, 1003, 'BOOKED', (SELECT seat_id FROM tb_seat WHERE venue_id = 1 AND `row` = 'C' AND col = '10'), NOW(), NOW());