# MOTIF

카메라로 스쿼트를 따라가며, 반복이 쌓일 때 앉는 깊이와 속도가 어떻게 달라지는지 찾아 주는 웹앱입니다. 인바디, 생활 습관 문진, 카메라로 잰 움직임을 함께 봅니다.

2026년 11월 14일 MEDILUX × 잇타 × UMC 하프 해커톤에서 4시간 동안 만들었습니다.

- 배포 서버는 해커톤이 끝난 뒤 내렸습니다. 아래 [실행](#실행) 방법으로 로컬에서 띄울 수 있고, API 문서는 띄운 서버의 `/swagger-ui.html`에서 볼 수 있습니다
- 화면 저장소 [hackathon-1114/frontend](https://github.com/hackathon-1114/frontend)

## 사용 흐름

| 단계 | 화면 | 하는 일 |
|---|---|---|
| 1 | 인바디 | 결과지 사진을 올리거나 가상 데이터를 넣어 근육량과 좌우 차이를 봅니다 |
| 2 | 문진 | 앉아 있는 시간, 불편한 부위, 생활 습관을 고릅니다 |
| 3 | 확인 영역 | 인바디와 문진을 묶어 카메라로 확인할 부위를 몸 그림에 표시합니다 |
| 4 | 카메라 | 스쿼트를 하면 횟수를 세고 깊이를 재며 음성으로 안내합니다 |
| 5 | 결과 | 몇 회째부터 달라졌는지, 보완 운동, 이전 세트와의 비교를 보여 줍니다 |

## 만든 방식

**자세 추적은 브라우저에서 합니다.** MediaPipe Pose Landmarker가 폰 안에서 관절 33개를 추적합니다. 영상은 서버로 보내지 않습니다. 한 프레임을 처리하는 데 약 10ms가 걸립니다(M4 Mac, Chrome 기준).

**횟수와 판정은 규칙으로 합니다.** 엉덩이 높이 비율로 반복을 세고, 반복마다 깊이, 걸린 시간, 상체 기울기, 무릎 간격을 기록합니다. 서버의 `SetAnalyzer`가 처음 두 번과 비교해 깊이가 5%p 넘게 얕아지거나 30% 넘게 느려진 첫 회차를 찾습니다.

**AI는 설명만 합니다.** Claude는 계산된 수치를 받아 문장으로 풀고 보완 운동을 제안합니다. 영상을 보고 자세를 판정하지 않습니다. 비전 모델의 운동 자세 판정은 정확도가 낮다는 연구가 있어 이렇게 나눴습니다. 근거는 [검증 기록](docs/verification.md)에 있습니다.

**서버는 단순합니다.** Spring Boot 3.5, Java 21, H2 인메모리 DB를 쓰고, 해커톤 동안 Railway 싱가포르 리전에 올려 시연했습니다. 화면은 빌드 도구 없는 정적 파일이고 서버가 함께 내려 줍니다. 로그인은 없고 `X-User-Id` 헤더로 사용자를 구분합니다.

```
폰 브라우저                               서버 (Spring Boot)
카메라 → MediaPipe → 반복별 수치   ──▶   세트 저장, 통계 계산
        음성 안내, 그래프          ◀──   통계, AI 설명 (Claude)
```

## 검증

- 공개 스쿼트 데이터셋(20명, 영상 36개)의 관절 좌표에 같은 규칙을 돌려 180회 중 180회를 맞게 셌습니다.
- 해커톤 현장에서 찍은 영상 두 개(측면 4회, 정면 3회)를 배포 사이트에 넣어 저장과 결과 반영까지 확인했습니다.
- 무릎이 안쪽으로 모이는 경우와 좌우 쏠림은 해당 사례가 없어 검증하지 못했습니다.
- 인바디 수치와 예시 사용자의 이전 세트는 가상 데이터입니다. 근전도 값은 센서가 없어 시뮬레이션이며 판단에 쓰지 않습니다.

자세한 내용은 [기능별 구현과 한계](docs/features.md)와 [검증 기록](docs/verification.md)에 있습니다.

## API

| 경로 | 내용 |
|---|---|
| `POST /api/inbody`, `GET /api/inbody` | 인바디. 가상 데이터, 직접 입력, 결과지 사진 읽기. 좌우 근육량 차이 계산 |
| `POST /api/surveys`, `GET /api/surveys/latest` | 문진 |
| `POST /api/body-alerts/analyze`, `GET /api/body-alerts/latest` | 인바디와 문진으로 확인할 영역 고르기 (약 10초) |
| `POST /api/sets`, `GET /api/sets` | 카메라로 잰 세트 저장. 통계를 바로 돌려줌 |
| `POST /api/sets/{id}/feedback` | 세트 수치를 AI가 설명 (약 15초) |
| `POST /api/insights/analyze`, `GET /api/insights` | 쌓인 기록에서 패턴 찾기 (약 12초) |
| `GET /api/overview` | 화면에 필요한 값을 한 번에 |

응답은 항상 `{ "success", "code", "message", "data" }` 모양입니다. 요청과 응답의 정확한 모양은 Swagger에서 볼 수 있습니다.

## 실행

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cp .env.example .env    # ANTHROPIC_API_KEY를 넣습니다. 없어도 서버는 뜨고 AI 호출만 503을 줍니다
./gradlew bootRun       # http://localhost:8080
./gradlew test
```

DB는 인메모리라 재시작하면 초기화되고, 예시 사용자(유저 1)의 데이터가 다시 들어갑니다.

## 폴더

| 위치 | 내용 |
|---|---|
| `src/main/resources/static` | 화면. `index.html`(1~3, 5단계), `pose-test.html`(4단계), `app.js`, `app.css` |
| `src/main/java/com/hackathon/domain` | 기능별 API. `exerciseset`(세트와 통계), `inbody`, `survey`, `bodyalert`, `insight` |
| `tools/verify` | 공개 데이터셋으로 횟수 규칙을 다시 돌려 보는 스크립트 |
| `docs` | 기능별 구현과 한계, 검증 기록 |
