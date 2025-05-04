## Event-System 로컬환경 Docker 사용 가이드

### docker-compose.yml 설정
```yml
version: '3.8'

services:
  redis-node-1:
    image: redis:7.2
    container_name: redis-node-1
    command: redis-server /usr/local/etc/redis/redis.conf
    ports:
      - "7001:7001"
      - "17001:17001"
    volumes:
      - ./redis/redis-node-1.conf:/usr/local/etc/redis/redis.conf
      - redis-data-1:/data
    networks:
      redis-cluster-net:
        aliases:
          - redis-node-1
    environment:
      TZ: Asia/Seoul  # 타임존 설정

  redis-node-2:
    image: redis:7.2
    container_name: redis-node-2
    command: redis-server /usr/local/etc/redis/redis.conf
    ports:
      - "7002:7002"
      - "17002:17002"
    volumes:
      - ./redis/redis-node-2.conf:/usr/local/etc/redis/redis.conf
      - redis-data-2:/data
    networks:
      redis-cluster-net:
        aliases:
          - redis-node-2
    environment:
      TZ: Asia/Seoul  # 타임존 설정

  redis-node-3:
    image: redis:7.2
    container_name: redis-node-3
    command: redis-server /usr/local/etc/redis/redis.conf
    ports:
      - "7003:7003"
      - "17003:17003"
    volumes:
      - ./redis/redis-node-3.conf:/usr/local/etc/redis/redis.conf
      - redis-data-3:/data
    networks:
      redis-cluster-net:
        aliases:
          - redis-node-3
    environment:
      TZ: Asia/Seoul  # 타임존 설정

  redis-cluster-entry:
    image: redis:7.2
    container_name: redis-cluster-init
    depends_on:
      - redis-node-1
      - redis-node-2
      - redis-node-3
    networks:
      - redis-cluster-net
    entrypoint: >
      sh -c "
        sleep 20 &&
        echo yes | redis-cli -a root --cluster create redis-node-1:7001 redis-node-2:7002 redis-node-3:7003 --cluster-replicas 0 &&
        sleep 5 &&
        redis-cli -a root -h redis-node-1 -p 7001 ACL SETUSER root on >root ~* +@all &&
        redis-cli -a root -h redis-node-2 -p 7002 ACL SETUSER root on >root ~* +@all &&
        redis-cli -a root -h redis-node-3 -p 7003 ACL SETUSER root on >root ~* +@all
      "
    environment:
      TZ: Asia/Seoul  # 타임존 설정

  postgres:
    image: postgres:14
    container_name: postgres-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: event-system
      TZ: Asia/Seoul  # 타임존 설정
    networks:
      - postgres-net
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  redis-data-1:
  redis-data-2:
  redis-data-3:
  postgres-data:

networks:
  redis-cluster-net:
    driver: bridge
  postgres-net:
    driver: bridge
```

## Docker Compose
### Docker Compose 파일 위치로 이동
```shell
cd [your-directory]/event-system/event-docker
```

### Docker Compose 실행
```shell
docker-compose up
```
### 또는 백그라운드 실행
```shell
docker-compose up -d
```
### Docker Compose 종료
```shell
docker-compose down
```

---

## PostgreSQL

### PostgreSQL 컨테이너 접속 -> PostgreSQL 클라이언트 실행
```shell
docker exec -it postgres-db psql -U postgres
```
- `docker exec -it postgres-db` : 실행 중인 postgres-db 컨테이너에 상호작용하는 터미널을 연결
- `psql` : PostgreSQL 클라이언트
- `-u postgres` : postgres 사용자 계정으로 접속 (docker-compose.yml의 POSTGRES_USER 설정과 동일해야 함)

### PostgreSQL 데이터베이스 목록 조회
```SQL
SELECT datname FROM pg_database;
```

### 특정 데이터베이스 사용 (mydb 예시)
```SQL
\c mydb
```

### 사용자 목록 조회
```SQL
\du
```

### 볼륨을 통한 데이터 영속성 설정 (PostgreSQL)
- postgres-data 볼륨을 /var/lib/postgresql/data에 마운트하여 PostgreSQL 데이터베이스 파일이 호스트 시스템에 영구 저장
-  PostgreSQL 컨테이너 종료 및 재시작 후에도 데이터 유지

---

## Redis
### 로컬 환경 구성시 /etc/hosts에 cluster-announce-ip 추가 필요
```shell
# 자동 스크립트 추가 (1, 2번이 수행됨)
./update-hosts.sh 2번 실행
- 1. chmod +x update-hosts.sh
- 2. ENTRY="127.0.0.1 redis-node-1 redis-node-2 redis-node-3" 추가

# 수동 추가: /etc/hosts 파일에 노드 IP 추가
sudo vi /etc/hosts
127.0.0.1 redis-node-1
127.0.0.1 redis-node-2
127.0.0.1 redis-node-3
```

### redis cluster Local 실행시 bind ip 주소 -> 자신의 로컬 ip 주소로 변경 필요
```
1. docker-compose.yml 파일에서 redis-cluster bind ip 주소 변경
2. redis -> node1, 2, 3 폴더에서 redis.conf 파일에서 bind ip 주소 변경
```

### Redis Streams 테스트 (예시)
#### Stream에 데이터 추가
- XADD: Redis의 Stream 타입에 데이터를 추가하는 명령어
- mystream: Stream 이름 (없으면 자동 생성됨)
- `*`: Redis가 자동으로 ID 생성함 (timestamp-sequence 형식)

#### test-id 1234 temperature 20: key-value 쌍으로 데이터 삽입
```shell
XADD mystream * test-id 1234 temperature 20
```

#### "mystream"이라는 스트림 저장 결과
```json
{
  "test-id": "1234",
  "temperature": "20"
}
```

#### Stream 데이터 읽기
- XRANGE: 스트림의 데이터를 범위 조회하는 명령어
- -: 스트림의 시작점
- +: 스트림의 끝점

#### "mystream" 스트림의 전체 데이터를 조회
```shell
XRANGE mystream - +
```

#### 볼륨을 통한 데이터 영속성 설정
- redis-data 볼륨을 /data에 마운트하여 Redis의 영속성 설정 (appendonly yes)이 적용됨
- Redis 종료 및 재시작 후에도 데이터 유지

---