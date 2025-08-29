
INSERT IGNORE INTO tb_event
(event_id, account_id, title, age_limit, description, genre, thumbnail_url, status, created_at, updated_at)
VALUES
    (1, 1, '뮤지컬 〈위키드〉 내한 공연(WICKED The Musical)', 12,
     '뮤지컬 〈위키드〉 내한 공연(WICKED The Musical) 뮤지컬 설명',
     '뮤지컬',
     'https://ticketimage.interpark.com/Play/image/large/25/25005777_p.gif',
     'APPROVED',
     NOW(), NOW()),

    (2, 2, '도자 캣 내한공연', 15,
     '도자 캣 내한공연 콘서트 설명',
     '콘서트',
     'https://ticketimage.interpark.com/Play/image/large/25/25012412_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (3, 3, '연극 〈프리마 파시〉', 15,
     '연극 〈프리마 파시〉 연극 설명',
     '연극',
     'https://ticketimage.interpark.com/Play/image/large/25/25009991_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (4, 4, '2025 전시지원］ 요시고 사진전 2 (~11.30까지 사용)', 12,
     '［2025 전시지원］ 요시고 사진전 2 전시회 설명',
     '전시회',
     'https://ticketimage.interpark.com/Play/image/large/25/25011411_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (5, 5, '뮤지컬 〈맘마미아!〉', 12,
     '뮤지컬 〈맘마미아!〉뮤지컬 설명',
     '뮤지컬',
     'https://ticketimage.interpark.com/Play/image/large/L0/L0000123_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (6, 6, '［2025 전시지원］ 워너 브롱크호스트', 12,
     '［2025 전시지원］ 워너 브롱크호스트 전시회 설명',
     '전시회',
     'https://ticketimage.interpark.com/Play/image/large/25/25011387_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (7, 7, '잉글리시 콘서트 〈헨델, 리날도〉', 12,
     '잉글리시 콘서트 〈헨델, 리날도〉클래식 설명',
     '클래식',
     'https://ticketimage.interpark.com/Play/image/large/25/25012245_p.gif',
     'APPROVED',
     NOW(), NOW()),

   (8, 8, '뮤지컬 〈쉐도우〉', 12,
     '뮤지컬 〈쉐도우> 뮤지컬 설명',
     '뮤지컬',
     'https://ticketimage.interpark.com/Play/image/large/25/25009941_p.gif',
     'APPROVED',
     NOW(), NOW());