# Hackathon Backend (Spring Boot)

MEDILUX × 잇타 × UMC 하프 해커톤용 백엔드 보일러플레이트.

- Java 21 / Spring Boot 3.5 / Gradle 8.14
- JPA + H2(로컬) / PostgreSQL(prod)
- Swagger, CORS 전체 허용, 공통 응답 포맷, 전역 예외 처리
- Claude API 래퍼 (`POST /api/ai/chat`)

## 실행

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew bootRun
```

API 키는 프로젝트 루트 `.env`에 `ANTHROPIC_API_KEY=sk-ant-...` 한 줄로 넣어두면 자동으로 읽는다 (git 제외, `.env.example` 참고). 키가 없어도 서버는 뜨고 AI만 503을 준다.

| URL | 설명 |
|---|---|
| http://localhost:8080/swagger-ui.html | API 문서 (프론트에 이 링크 공유) |
| http://localhost:8080/h2-console | DB 콘솔. JDBC URL `jdbc:h2:mem:hackathon`, user `sa` |
| http://localhost:8080/api/health | 헬스 체크 |

로컬 DB는 인메모리라 재시작하면 초기화되고, `SeedData`가 데모 데이터를 다시 넣는다.

## 응답 포맷

```json
{ "success": true,  "code": "OK",        "message": "성공", "data": { } }
{ "success": false, "code": "SAMPLE404", "message": "샘플을 찾을 수 없습니다." }
```

검증 실패(400)는 `data`에 `{ 필드명: 메시지 }`가 들어간다.

## 현장에서 새 도메인 추가하는 법

1. `domain/sample` 폴더를 통째로 복사해서 `domain/<이름>`으로 바꾼다
2. 클래스명/필드 변경 (IntelliJ에서 Rename 리팩터링)
3. 필요한 에러는 `ErrorCode`에 한 줄 추가
4. 데모 데이터는 `global/config/SeedData`에 추가

인증은 없다. 유저 구분이 필요하면 `X-User-Id` 헤더를 받는다 (`SampleController.create` 참고).

## AI 호출

```bash
curl -X POST localhost:8080/api/ai/chat -H 'Content-Type: application/json' -d '{
  "system": "너는 복약 안내 도우미야. 한국어로 3줄 이내로 답해.",
  "message": "타이레놀이랑 감기약 같이 먹어도 돼?",
  "history": []
}'
# → { "success": true, "data": { "reply": "..." } }
```

프론트가 화면에 바로 쓸 구조화된 데이터가 필요하면 `/api/ai/json`에 원하는 모양(`schema`)을 같이 보낸다. `required`와 `additionalProperties`는 생략해도 서버가 채운다.

```bash
curl -X POST localhost:8080/api/ai/json -H 'Content-Type: application/json' -d '{
  "system": "너는 증상 분류 도우미야. 진단이 아니라 참고용 안내만 해.",
  "message": "이틀째 38도 열이 나고 목이 많이 아파",
  "schema": { "properties": {
    "risk": { "type": "string", "enum": ["낮음", "보통", "높음"] },
    "department": { "type": "string" },
    "tips": { "type": "array", "items": { "type": "string" } }
  }}
}'
# → { "success": true, "data": { "risk": "보통", "department": "이비인후과 또는 내과", "tips": [...] } }
```

- 챗봇처럼 이어가려면 이전 대화를 `history`에 `{role: "user"|"assistant", content}`로 넣어 보낸다
- 모델/속도는 환경변수 `ANTHROPIC_MODEL`(기본 `claude-opus-5-5`), `ANTHROPIC_EFFORT`(기본 `low`)
- 키가 없으면 `AI503`, 한도 초과는 `AI429`

## 배포 (Railway 기준)

1. GitHub에 푸시 → Railway에서 New Project → Deploy from GitHub repo (Dockerfile 자동 인식)
2. 같은 프로젝트에 PostgreSQL 추가
3. 서비스 Variables 설정
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}`
   - `DB_USERNAME=${{Postgres.PGUSER}}`, `DB_PASSWORD=${{Postgres.PGPASSWORD}}`
   - `ANTHROPIC_API_KEY=...`
4. Settings → Networking → Generate Domain → `https://<도메인>/swagger-ui.html` 확인

`PORT`는 Railway가 주입하고 `application.yml`이 그대로 읽는다. DB 붙이기 귀찮으면 `SPRING_PROFILES_ACTIVE`를 비워 두면 H2로 뜬다 (재배포 시 데이터 초기화).
