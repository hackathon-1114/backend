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
    FORMCHECK_TOO_LARGE(HttpStatus.BAD_REQUEST, "FORMCHECK400",
            "장면 사진이 너무 큽니다. 긴 변 768px 정도로 줄여서 보내 주세요."),
    FORMCHECK_NOT_ANALYZABLE(HttpStatus.UNPROCESSABLE_ENTITY, "FORMCHECK422",
            "영상에서 운동하는 모습을 찾지 못했습니다. 전신이 보이게 다시 찍어 주세요."),
    INSIGHT_NOT_ENOUGH_DATA(HttpStatus.BAD_REQUEST, "INSIGHT400",
            "분석할 기록이 부족합니다. 카메라 세트를 1번 이상 저장하거나 운동 기록을 5개 이상 남겨 주세요."),
    SET_NOT_FOUND(HttpStatus.NOT_FOUND, "SET404", "세트 기록을 찾을 수 없습니다."),
    INBODY_EMPTY(HttpStatus.BAD_REQUEST, "INBODY400", "가상 데이터(sample), 수치(values), 사진(image) 중 하나를 보내 주세요."),
    INBODY_NOT_READABLE(HttpStatus.UNPROCESSABLE_ENTITY, "INBODY422",
            "사진에서 인바디 수치를 읽지 못했습니다. 결과지 전체가 선명하게 보이게 다시 찍어 주세요."),
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY404", "작성한 문진이 없습니다."),
    AI_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI503", "ANTHROPIC_API_KEY가 설정되지 않았습니다."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI429", "AI 요청이 너무 많습니다. 잠시 후 다시 시도하세요."),
    AI_REFUSED(HttpStatus.UNPROCESSABLE_ENTITY, "AI422", "AI가 이 요청에 답변하지 않았습니다."),
    AI_ERROR(HttpStatus.BAD_GATEWAY, "AI502", "AI 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
