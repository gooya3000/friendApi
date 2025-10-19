# APR Backend Assignment — Friends API

> 에이피알 백엔드 직무 과제 구현물입니다.  
Spring Boot 3.5, Java 21, Gradle, H2를 기반으로 하며, Layered Architecture(Controller → Service → Repository) 구조를 따릅니다.

---

## 🚀 실행 방법 (Run)

### 1. 요구사항
- Java 21 이상
- Gradle Wrapper 포함

### 2. 로컬 실행

```bash
./gradlew clean bootRun
```

### 3. 빌드 & 실행 (jar)

```bash
./gradlew clean build
java -jar build/libs/apr-backend-assignment-*.jar
```

### 4. H2 콘솔
- URL: http://localhost:8080/h2-console  
- JDBC URL: `jdbc:h2:mem:testdb`  
- 초기 데이터: `schema.sql`, `data.sql` 자동 로드

### 5. Swagger UI
- http://localhost:8080/swagger-ui/index.html  
- API 명세서 확인 가능

### 6. 공통 요청 헤더
- `X-user-id: <사용자 ID>` (필수)

---

## 📡 API 요약

### 1. 친구 목록 조회  
- **GET** `/api/friends?page=0&maxSize=20&sort=approvedAt,desc`  
- 나 기준 현재 친구 목록 조회 (`approvedAt` 정렬 지원)

### 2. 받은 친구 신청 목록 조회  
- **GET** `/api/friends/requests?maxSize=20&window=1d&sort=requestedAt,desc`  
- 슬라이딩 윈도우(`1d|7d|30d|90d|over`) 적용 및 최신순 정렬

### 3. 친구 신청  
- **POST** `/api/friends/request`  
- 헤더: `X-user-id` 필수  
- 초당 10회 제한(429 처리), 자기 자신/존재하지 않는 사용자/중복 요청 방지

### 4. 친구 수락  
- **POST** `/api/friends/accept/{requestId}`  
- 친구 관계 생성 및 상태 전이 검증

### 5. 친구 거절  
- **POST** `/api/friends/reject/{requestId}`  
- 요청 삭제 및 상태 전이 검증

---

## 🧠 핵심 로직 설명

### 📍 Rate Limiting
- 사용자 기준 초당 10회 요청 제한
- 초과 시 `429 Too Many Requests` 반환

### 📍 Idempotency & 동시성 제어
- `(fromUserId, toUserId, status=PENDING)` 유니크 제약으로 중복 방지  
- 상태 전이(PENDING → ACCEPTED/REJECTED) 원자화  
- 멱등 응답 또는 409 반환 정책 선택

### 📍 슬라이딩 윈도우 조회
- `window` 값에 따라 `requestedAt` 기준 범위 필터링  
- 최신순 정렬 후 `maxSize` 제한 반환

### 📍 공통 응답 포맷

```json
{
  "code": "OK",
  "message": "success",
  "data": { ... }
}
```

---

## 🛠️ 폴더 구조

```
/src
  /main
    /java/.../controller     # REST 엔드포인트
    /java/.../service        # 비즈니스 로직
    /java/.../repository     # JPA 리포지토리
    /java/.../domain         # 엔티티/DTO
    /java/.../config         # 컨피그
    /resources
      application.yml
      schema.sql
      data.sql
```

---

## 🧪 예시 cURL

### 친구 신청

```bash
curl -X POST http://localhost:8080/api/friends/request   -H 'Content-Type: application/json'   -H 'X-user-id: 1001'   -d '{"toUserId":1002}'
```

### 받은 요청 목록 (최근 7일)

```bash
curl 'http://localhost:8080/api/friends/requests?maxSize=20&window=7d&sort=requestedAt,desc'   -H 'X-user-id: 1002'
```

### 친구 수락

```bash
curl -X POST http://localhost:8080/api/friends/accept/{requestId}   -H 'X-user-id: 1002'
```

---

## ✅ 체크리스트

- [x] 친구 신청/수락/거절/목록 API 구현  
- [x] Rate Limiter 적용 (초당 10회)  
- [x] 슬라이딩 윈도우 조회 구현  
- [x] H2 스키마/데이터 포함  
- [x] Swagger UI 명세 제공  
- [x] README에 실행 방법 및 핵심 로직 설명 포함  
