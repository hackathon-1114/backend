# Backend (Spring Boot)

운동 중 자세와 움직임 변화를 기록하고, 쌓인 기록에서 사용자가 몰랐던 패턴을 찾아 주는 서비스의 백엔드. 프론트 저장소는 [hackathon-1114/frontend](https://github.com/hackathon-1114/frontend).

## 문서

- [docs/features.md](docs/features.md): 기능별 구현 방법과 한계, 화면과 API의 대응
- [docs/verification.md](docs/verification.md): 자세 감지를 공개 데이터로 검증한 결과와 다시 돌려 보는 법

## 지금 있는 API

| 경로 | 내용 |
|---|---|
| `POST /api/surveys`, `GET /api/surveys/latest` | 사용자 문진 (목표, 불편한 부위와 상황) |
| `POST /api/inbody`, `GET /api/inbody` | 인바디. 결과지 사진을 AI가 읽거나 수치를 직접 입력. 좌우 근육량 차이 계산 |
| `POST /api/sets`, `GET /api/sets` | 카메라로 추적한 세트 저장. 평균 깊이, 후반 변화, 무너진 회차를 바로 계산 |
| `POST /api/sets/{id}/feedback` | 세트 수치를 인바디, 문진과 함께 AI가 설명 (3~13초) |
| `POST /api/insights/analyze`, `GET /api/insights` | 쌓인 기록 전체에서 패턴 3개를 찾음 (약 12초) |
| `/api/workouts` | 운동 기록 CRUD (종류, 시각, 시간, 거리, 강도, 기분, 메모) |
| `POST /api/form-checks`, `GET /api/form-checks` | 영상에서 뽑은 장면을 AI가 직접 봄. 보조 기능 |
| `/api/ai/chat`, `/api/ai/json` | 범용 AI 호출 (사진 입력 가능) |
| `/api/samples` | 새 도메인을 만들 때 복사하는 CRUD 템플릿 |

유저 1에는 운동 기록 15건, 세트 3건, 자세 점검 5건, 인바디 1건, 문진 1건이 시드로 들어 있다. 정확한 요청과 응답 모양은 Swagger에서 본다.

## 프론트가 가져다 쓸 코드

서버가 띄우는 시험 페이지 두 개에 프론트용 코드가 들어 있다.

- `/pose-test.html`: 카메라나 영상 파일에서 MediaPipe로 관절을 추적해 스쿼트 횟수, 깊이, 상체 기울기, 무릎 간격을 실시간으로 재고 음성으로 안내한다. 세트를 저장하고 AI 설명을 받는 흐름까지 들어 있다. 소스의 `measure`, `update`, `findings`가 핵심이다
- `/form-check.html`: 영상에서 장면을 뽑아 `POST /api/form-checks`로 보낸다. 소스의 `extractFrames`가 핵심이다

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

검증 실패(400)는 `data`에 `{ 필드명: 메시지 }`가 들어간다. 잘못된 메서드나 필수 헤더 누락은 `COMMON405`, `COMMON400`처럼 상태코드가 붙은 코드로 내려간다.

`/api` 요청은 `POST /api/samples → 201 (12ms)` 형태로 서버 로그에 한 줄씩 남는다. 프론트 연동이 안 될 때 배포 로그에서 먼저 확인한다.

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

사진을 같이 보내려면 두 엔드포인트 모두 `images`에 base64를 넣는다 (약 봉투, 처방전, 식단 사진 등). multipart가 아니라 JSON 그대로다.

```bash
curl -X POST localhost:8080/api/ai/json -H 'Content-Type: application/json' -d '{
  "message": "이 약 봉투에 적힌 약 이름과 복용법을 정리해줘",
  "images": [{ "mediaType": "image/jpeg", "data": "<base64 또는 data:image/jpeg;base64,...>" }],
  "schema": { "properties": {
    "drugs": { "type": "array", "items": { "properties": {
      "name": { "type": "string" }, "howToTake": { "type": "string" } } } }
  }}
}'
```

- 챗봇처럼 이어가려면 이전 대화를 `history`에 `{role: "user"|"assistant", content}`로 넣어 보낸다
- `images`는 요청당 최대 5장, 장당 원본 5MB 이하, `image/jpeg|png|gif|webp`. 프론트의 `FileReader.readAsDataURL` 결과를 그대로 넣어도 된다
- 요청마다 `"model": "opus" | "sonnet" | "haiku"`로 모델을 고를 수 있다. 생략하면 서버 기본값을 쓴다
- 서버 기본값은 환경변수 `ANTHROPIC_MODEL`(기본 `claude-opus-5-5`), 속도는 `ANTHROPIC_EFFORT`(기본 `low`)
- 안전 분류기가 요청을 거절하면 서버가 대체 모델로 자동 재시도한다. 그래도 거절되면 `AI422`
- 키가 없으면 `AI503`, 한도 초과는 `AI429`, 그 밖의 호출 실패(잔액 부족 포함)는 `AI502`이고 원인은 서버 로그에 남는다

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
