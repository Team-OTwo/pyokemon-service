
INSERT IGNORE INTO tb_price (event_schedule_id, seat_class_id, price)
SELECT 1, seat_class_id, price FROM (
                                        SELECT 'VIP' AS class_name, 198000 AS price
                                        UNION ALL
                                        SELECT 'R',   178000
                                        UNION ALL
                                        SELECT 'A',   148000
                                        UNION ALL
                                        SELECT 'B',   128000
                                    ) AS p
                                        JOIN tb_seat_class sc ON sc.class_name = p.class_name;
