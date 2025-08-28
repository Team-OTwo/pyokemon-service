INSERT IGNORE INTO tb_tenant
(tenant_id, account_id, name, corp_id, city, street, zipcode, ceo, created_at, updated_at)
VALUES
    (1, 1, '인터파크', '123-45-67890', '서울특별시', '강남구 테헤란로 152', '06236', '정창훈', NOW(), NOW());
