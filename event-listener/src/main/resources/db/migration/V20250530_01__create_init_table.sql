SET
TIMEZONE = 'Asia/Seoul';

---------------------- AccessCategory 테이블 ----------------------
CREATE TABLE access_category
(
    id            BIGINT       NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    category_code VARCHAR(50)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);

-- 인덱스 추가
CREATE INDEX idx_access_category_code ON access_category (category_code);
CREATE INDEX idx_access_category_is_deleted ON access_category (is_deleted);

-- AccessCategory 테이블에 대한 주석
COMMENT
ON TABLE access_category IS '접근 카테고리 데이터';
COMMENT
ON COLUMN access_category.category_code IS '접근 카테고리 코드';
COMMENT
ON COLUMN access_category.name IS '접근 카테고리 이름';
COMMENT
ON COLUMN access_category.description IS '접근 카테고리 설명';
COMMENT
ON COLUMN access_category.created_at IS '카테고리 생성 일시';
COMMENT
ON COLUMN access_category.updated_at IS '카테고리 업데이트 일시';
COMMENT
ON COLUMN access_category.is_deleted IS '삭제 여부';


---------------------- AccessEvent 테이블 ----------------------
CREATE TABLE access_event
(
    id              BIGINT           NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    category_id     BIGINT           NOT NULL, -- AccessCategory의 id와 연관
    category_code   VARCHAR(50)      NOT NULL, -- AccessCategory와 연관
    user_id         VARCHAR(255)     NOT NULL,
    endpoint        VARCHAR(255)     NOT NULL,
    http_method     VARCHAR(10)      NOT NULL CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH')),
    response_status INT              NOT NULL,
    response_time   DOUBLE PRECISION NOT NULL,
    ip_address      VARCHAR(50)      NOT NULL,
    user_agent      TEXT NULL,                 -- 클라이언트 및 브라우저 정보
    inputs          TEXT NULL,                 -- 요청 파라미터 (JSON 형태)
    outputs         TEXT NULL,                 -- 요청 바디 (JSON 형태)
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래 키 추가 (category_id와 category_code 둘 다 참조)
    CONSTRAINT fk_access_event_category_id FOREIGN KEY (category_id)
        REFERENCES access_category (id)
        ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_access_event_category_id ON access_event (category_id);
CREATE INDEX idx_access_event_category_code ON access_event (category_code);
CREATE INDEX idx_access_event_user_id ON access_event (user_id);
CREATE INDEX idx_access_event_http_method ON access_event (http_method);
CREATE INDEX idx_access_event_created_at ON access_event (created_at);
CREATE INDEX idx_access_event_response_status ON access_event (response_status);

-- AccessEvent 테이블에 대한 주석
COMMENT
ON TABLE access_event IS 'API 접근 이벤트 데이터';
COMMENT
ON COLUMN access_event.category_id IS '접근 카테고리 ID (외래 키)';
COMMENT
ON COLUMN access_event.category_code IS '접근 카테고리 코드 (외래 키)';
COMMENT
ON COLUMN access_event.user_id IS 'API 요청 사용자 ID';
COMMENT
ON COLUMN access_event.endpoint IS '요청 엔드포인트 (URI)';
COMMENT
ON COLUMN access_event.http_method IS 'HTTP 요청 방식';
COMMENT
ON COLUMN access_event.response_status IS 'HTTP 응답 상태 코드';
COMMENT
ON COLUMN access_event.response_time IS 'HTTP 응답 시간 (ms, 소수점 포함)';
COMMENT
ON COLUMN access_event.ip_address IS '요청 IP 주소';
COMMENT
ON COLUMN access_event.user_agent IS '클라이언트 User-Agent 정보';
COMMENT
ON COLUMN access_event.inputs IS '요청 파라미터 (JSON 형태)';
COMMENT
ON COLUMN access_event.outputs IS '응답 파라미터 (JSON 형태)';
COMMENT
ON COLUMN access_event.created_at IS '이벤트 생성 일시';