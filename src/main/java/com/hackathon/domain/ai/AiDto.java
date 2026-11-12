package com.hackathon.domain.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

public class AiDto {

    /**
     * @param system  역할/규칙 지시 (선택). 예: "너는 복약 안내 도우미야. 반드시 한국어로 3줄 이내로 답해."
     * @param message 이번 사용자 입력
     * @param history 이전 대화 (선택). 챗봇처럼 이어서 대화할 때 프론트가 그대로 다시 보내면 됨
     */
    public record ChatRequest(
            String system,
            @NotBlank String message,
            List<@Valid Turn> history) {
    }

    public record Turn(
            @Pattern(regexp = "user|assistant") String role,
            @NotBlank String content) {
    }

    public record ChatResponse(String reply) {
    }

    /**
     * @param schema 받고 싶은 응답 모양 (JSON Schema). required/additionalProperties는 생략해도 서버가 채움.
     *               예: {"properties": {"risk": {"type": "string", "enum": ["낮음","보통","높음"]},
     *                                  "department": {"type": "string"}}}
     */
    public record JsonRequest(
            String system,
            @NotBlank String message,
            List<@Valid Turn> history,
            @NotEmpty Map<String, Object> schema) {
    }
}
