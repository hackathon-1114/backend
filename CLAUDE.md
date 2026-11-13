# 해커톤 백엔드 작업 규칙

4시간짜리 해커톤용 Spring Boot 백엔드다. 목표는 배포된 서버에서 끝까지 도는 데모이고, 속도가 구조보다 우선한다. 사용법과 curl 예시는 `README.md`에 있다.

## 명령

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew test       # 변경 후 항상 실행
./gradlew bootRun    # http://localhost:8080/swagger-ui.html
```

`master`에 푸시하면 Railway가 1~2분 안에 자동 배포한다. 배포 주소는 https://hackathon-backend-production-967eabf5.up.railway.app 이다.

변경 요청을 받으면 구현하고 `./gradlew test` 통과를 확인한 뒤, 묻지 않고 커밋해서 `origin/master`에 푸시한다. 푸시 후에는 배포 주소에서 바뀐 엔드포인트를 호출해 확인한다. 테스트가 실패하면 푸시하지 않고 보고한다. force push와 히스토리 수정은 하지 않는다.

## 새 도메인 추가

`domain/sample`이 템플릿이다. 새 기능은 이 구조를 그대로 복사해서 만든다.

1. `domain/<이름>/{controller,dto,entity,repository,service}`를 `sample`과 같은 모양으로 만든다
2. 컨트롤러는 `ApiResponse.ok(...)`로 감싸 반환하고 `@Tag`, `@Operation`을 단다 (프론트는 Swagger만 본다)
3. 실패는 `throw new BusinessException(ErrorCode.X)`로 던진다. 필요한 코드는 `ErrorCode`에 `<도메인><HTTP상태>` 형식으로 추가한다 (예: `SAMPLE404`)
4. 요청 DTO는 record와 Bean Validation으로 검증한다
5. 데모에 보일 데이터는 `global/config/SeedData`에 추가한다
6. 새 엔드포인트마다 `ApiSmokeTest`에 MockMvc 테스트를 하나 추가한다

## 지킬 것

- 인증은 없다. 유저 구분은 `X-User-Id` 헤더로 한다 (`SampleController.create` 참고)
- DB는 H2 인메모리이고 재시작하면 초기화된다. 엔티티는 자유롭게 바꿔도 되지만 데이터는 `SeedData`로 복구돼야 한다
- AI 기능은 `AiService`의 `chat`, `json`을 서비스에서 호출해 만든다. Anthropic 클라이언트를 새로 만들지 않는다
- 응답 원문과 요청 본문에는 건강 정보가 섞일 수 있으니 로그에 남기지 않는다
- 의존성 추가, 패키지 구조 변경, 공통 응답 포맷 변경은 요청이 있을 때만 한다
- 프론트에 영향을 주는 변경(경로, 필드명, 에러 코드)은 답변에서 따로 알린다
