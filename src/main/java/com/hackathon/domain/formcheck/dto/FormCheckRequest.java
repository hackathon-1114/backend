package com.hackathon.domain.formcheck.dto;

import com.hackathon.domain.ai.AiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class FormCheckRequest {

    /** AI는 영상 파일을 직접 받지 못한다. 프론트가 영상에서 장면을 뽑아 사진으로 보낸다 (static/form-check.html 참고) */
    public record Create(
            @Schema(description = "운동 종목", example = "스쿼트")
            @NotBlank @Size(max = 30) String exercise,
            @Schema(description = "영상에서 시간 순서로 뽑은 장면 1~12장. 긴 변 768px 정도의 JPEG 권장")
            @NotEmpty @Size(max = MAX_FRAMES) List<AiDto.@Valid Image> frames,
            @Schema(description = "영상을 찍은 시각 (한국 시간). 생략하면 지금", example = "2026-11-14T19:30:00")
            LocalDateTime performedAt,
            @Schema(description = "특히 봐 줬으면 하는 점 (선택)", example = "무릎이 안으로 모이는지 봐 주세요")
            @Size(max = 300) String memo) {
    }

    public static final int MAX_FRAMES = 12;
}
