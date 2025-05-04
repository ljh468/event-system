-- AccessCategory 더미 데이터 INSERT
INSERT INTO access_category (category_code, name, description, created_at)
VALUES ('UNKNOWN', 'UNKNOWN 서비스', 'UNKNOWN 접근 이벤트', CURRENT_TIMESTAMP),
       ('API_PUBLIC', 'Admin API 서비스', '관리자용 API 엔드포인트 접근 이벤트', CURRENT_TIMESTAMP),
       ('API_ADMIN', 'Admin API 서비스', '관리자용 API 엔드포인트 접근 이벤트', CURRENT_TIMESTAMP),
       ('API_EXTERNAL', '외부 파트너 API', '외부 파트너용 API 접속 이벤트', CURRENT_TIMESTAMP),
       ('API_INTERNAL', '내부 파트너 API', '내부 파트너용 API 접속 이벤트', CURRENT_TIMESTAMP),
       ('HEALTH_SYSTEM', '시스템 상태 확인 헬스체크', '시스템 헬스체크 API 접근 이벤트', CURRENT_TIMESTAMP),
       ('SERVICE_QUEUE', '서비스 큐(API 작업 큐)', '작업 큐 서비스 접근용 이벤트', CURRENT_TIMESTAMP);

-- AccessEvent 더미 데이터 INSERT
INSERT INTO access_event (category_id, category_code, user_id, endpoint, http_method, response_status,
                          response_time, ip_address, user_agent, inputs, outputs, created_at)
VALUES
    -- 일반 API 이벤트 (PUBLIC)
    ((SELECT id FROM access_category WHERE category_code = 'API_PUBLIC'), 'API_PUBLIC', 'user123', '/api/v1/tasks',
     'GET', 200, 150.234, '192.168.1.101',
     'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
     NULL, NULL, CURRENT_TIMESTAMP),

    ((SELECT id FROM access_category WHERE category_code = 'API_PUBLIC'), 'API_PUBLIC', 'user456', '/api/v1/users',
     'POST', 401, 180.543, '192.168.1.102',
     'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36',
     NULL, NULL, CURRENT_TIMESTAMP),

    ((SELECT id FROM access_category WHERE category_code = 'API_PUBLIC'), 'API_PUBLIC', 'user789', '/api/v1/orders',
     'GET', 404, 120.887, '192.168.1.103',
     'Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0',
     NULL, NULL, CURRENT_TIMESTAMP),

    -- 관리자 API 이벤트 (ADMIN)
    ((SELECT id FROM access_category WHERE category_code = 'API_ADMIN'), 'API_ADMIN', 'admin001', '/admin/v1/dashboard',
     'POST', 403, 100.123, '192.168.1.104',
     'Chrome/90.0.4430.212 Safari/537.36',
     NULL, NULL, CURRENT_TIMESTAMP),

    ((SELECT id FROM access_category WHERE category_code = 'API_ADMIN'), 'API_ADMIN', 'admin002', '/admin/v1/users',
     'GET', 200, 210.567, '192.168.1.105',
     'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:91.0) Gecko/20100101 Firefox/91.0',
     NULL, NULL, CURRENT_TIMESTAMP);