
INSERT IGNORE INTO tb_event_schedule
(event_schedule_id, event_id, venue_id, ticket_open_at, event_date, created_at, updated_at)
VALUES
    (1, 1, 1, '2025-08-28', '2025-09-20', NOW(), NOW()),
    (2, 2, 1, '2025-08-29', '2025-09-22', NOW(), NOW()),
    (3, 3, 1, '2025-08-28', '2025-09-20', NOW(), NOW()),
    (4, 4, 1, '2025-08-28', '2025-09-20', NOW(), NOW());