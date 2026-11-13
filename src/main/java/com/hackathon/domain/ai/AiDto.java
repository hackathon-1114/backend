package com.hackathon.domain.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
            List<@Valid Turn> history,
            @Size(max = 5) List<@Valid Image> images) {
    }

    public record Turn(
            @Pattern(regexp = "user|assistant") String role,
            @NotBlank String content) {
    }

    /**
     * 이번 message와 함께 보낼 사진 (선택). 예: 약 봉투, 처방전, 식단 사진.
     *
     * @param mediaType image/jpeg, image/png, image/gif, image/webp 중 하나
     * @param data      base64 문자열. "data:image/png;base64,..." 형태로 보내도 됨. 원본 5MB 이하
     */
    public record Image(
            @NotBlank @Pattern(regexp = "image/(jpeg|png|gif|webp)") String mediaType,
            @NotBlank @Size(max = MAX_IMAGE_BASE64_LENGTH, message = "이미지는 5MB 이하여야 합니다.") String data) {
    }

    /** 5MB를 base64로 인코딩한 길이 + data URL 접두어 여유분 */
    static final int MAX_IMAGE_BASE64_LENGTH = 6_990_600;

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
            @Size(max = 5) List<@Valid Image> images,
            @NotEmpty Map<String, Object> schema) {
    }
}
