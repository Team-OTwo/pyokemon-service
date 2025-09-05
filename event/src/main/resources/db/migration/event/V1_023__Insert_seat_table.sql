-- S석 200석 (venue_id=1, seat_class_id=1, floor=1)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE s AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM s WHERE n < 199
)
SELECT
    1,
    1,
    1,
    CHAR(ASCII('A') + (n DIV 20)),
    LPAD((n MOD 20) + 1, 2, '0')
FROM s;

-- R석 300석 (venue_id=1, seat_class_id=2, floor=1)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE r AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM r WHERE n < 299
)
SELECT
    1,
    2,
    1,
    CHAR(ASCII('A') + (n DIV 20)),
    LPAD((n MOD 20) + 1, 2, '0')
FROM r;

-- A석 300석 (venue_id=1, seat_class_id=3, floor=2)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE a AS (
    SELECT 0 AS n
    UNION ALL
    SELECT n + 1 FROM a WHERE n < 299
)
SELECT
    1,
    3,
    2,
    CHAR(ASCII('A') + (n DIV 20)),
    LPAD((n MOD 20) + 1, 2, '0')
FROM a;

-- B석 (A~I행: 14좌석, J~O행: 26좌석, venue_id=1, seat_class_id=4, floor=2)
INSERT INTO tb_seat (venue_id, seat_class_id, floor, `row`, col)
WITH RECURSIVE seat_rows AS (
    SELECT 0 AS r
    UNION ALL
    SELECT r + 1 FROM seat_rows WHERE r < 14
),
seat_cols AS (
    SELECT 1 AS c
    UNION ALL
    SELECT c + 1 FROM seat_cols WHERE c < 26
)
SELECT
    1,
    4,
    2,
    CHAR(ASCII('A') + r),
    LPAD(c, 2, '0')
FROM seat_rows
JOIN seat_cols ON (
    (r < 9 AND c <= 14)  -- A~I행 → 14좌석
    OR
    (r >= 9 AND c <= 26) -- J~O행 → 26좌석
);
