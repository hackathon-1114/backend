package com.hackathon.domain.bodyalert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record BodyAlertResponse(
        Long id,
        @Schema(description = "사전 분석 한두 문장 요약")
        String summary,
        @Schema(description = "카메라로 확인하면 좋을 영역 2~3개")
        List<Area> areas,
        @Schema(description = "다음 단계(카메라 측정)에서 무엇을 확인할지")
        String nextStep,
        LocalDateTime createdAt) {

    public record Area(
            @Schema(description = "영역 이름", example = "하체 좌우 균형")
            String title,
            @Schema(description = "바디맵에서 강조할 부위. 어깨 / 허리 / 골반 / 고관절 / 무릎 / 발목 / 전신", example = "무릎")
            String bodyPart,
            @Schema(description = "왼쪽 / 오른쪽 / 양쪽", example = "왼쪽")
            String side,
            @Schema(description = "추가 확인 권장 / 측정 필요 / 참고", example = "추가 확인 권장")
            String status,
            @Schema(description = "그렇게 본 근거. 인바디 수치나 문진 답을 인용한다")
            String reason,
            @Schema(description = "카메라 측정에서 확인하면 좋은 패턴")
            List<String> checkPatterns) {
    }
}
