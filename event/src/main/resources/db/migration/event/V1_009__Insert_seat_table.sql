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
    LPAD((n MOD 10) + 1, 2, '0')
FROM Seats;

-- R석 120석 생성 (venue_id=1, seat_class_id=2)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE Seats(n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1 FROM Seats WHERE n < 119
)
SELECT
    1,
    2,
    1,
    CHAR(ASCII('A') + (n DIV 10)),
    LPAD((n MOD 10) + 1, 2, '0')
FROM Seats;

-- A석 120석 생성 (venue_id=1, seat_class_id=3)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE Seats(n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1 FROM Seats WHERE n < 119
)
SELECT
    1,
    3,
    2,
    CHAR(ASCII('A') + (n DIV 10)),
    LPAD((n MOD 10) + 1, 2, '0')
FROM Seats;

-- B석 120석 생성 (venue_id=1, seat_class_id=4)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE Seats(n) AS (
    SELECT 0
    UNION ALL
    SELECT n + 1 FROM Seats WHERE n < 119
)
SELECT
    1,
    4,
    2,
    CHAR(ASCII('A') + (n DIV 10)),
    LPAD((n MOD 10) + 1, 2, '0')
FROM Seats;