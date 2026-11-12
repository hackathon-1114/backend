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
    AI_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "ANTHROPIC_API_KEY가 설정되지 않았습니다."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI429", "AI 요청이 너무 많습니다. 잠시 후 다시 시도하세요."),
    AI_REFUSED(HttpStatus.UNPROCESSABLE_ENTITY, "AI422", "AI가 이 요청에 답변하지 않았습니다."),
    AI_ERROR(HttpStatus.BAD_GATEWAY, "AI502", "AI 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
