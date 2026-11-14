package com.hackathon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다."),

    // 도메인별 에러는 아래에 추가
    SAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "SAMPLE404", "샘플을 찾을 수 없습니다."),
    WORKOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT404", "운동 기록을 찾을 수 없습니다."),
    FORMCHECK_NOT_FOUND(HttpStatus.NOT_FOUND, "FORMCHECK404", "자세 점검 기록을 찾을 수 없습니다."),
    FORMCHECK_NOT_ANALYZABLE(HttpStatus.UNPROCESSABLE_ENTITY, "FORMCHECK422",
            "영상에서 운동하는 모습을 찾지 못했습니다. 전신이 보이게 다시 찍어 주세요."),
    INSIGHT_NOT_ENOUGH_DATA(HttpStatus.BAD_REQUEST, "INSIGHT400", "운동 기록이 5개 이상 있어야 패턴을 분석할 수 있습니다."),
    AI_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "ANTHROPIC_API_KEY가 설정되지 않았습니다."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI429", "AI 요청이 너무 많습니다. 잠시 후 다시 시도하세요."),
    AI_REFUSED(HttpStatus.UNPROCESSABLE_ENTITY, "AI422", "AI가 이 요청에 답변하지 않았습니다."),
    AI_ERROR(HttpStatus.BAD_GATEWAY, "AI502", "AI 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
