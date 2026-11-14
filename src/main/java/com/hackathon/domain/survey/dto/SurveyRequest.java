package com.hackathon.domain.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SurveyRequest {

    public record Create(
            @Schema(description = "운동 목표", example = "하체 근력 강화")
            @Size(max = 100) String goal,
            @Schema(description = "운동 경력", example = "헬스 6개월")
            @Size(max = 30) String experience,
            @Schema(description = "불편함을 느끼는 부위와 상황. 없으면 빈 배열")
            @Size(max = 10) List<@Valid Discomfort> discomforts,
            @Schema(description = "그 밖에 알리고 싶은 것", example = "오른쪽 발목을 작년에 삔 적이 있음")
            @Size(max = 500) String memo) {
    }

    public record Discomfort(
            @Schema(description = "부위", example = "왼쪽 무릎")
            @NotBlank @Size(max = 30) String bodyPart,
            @Schema(description = "어떤 동작이나 상황에서", example = "계단을 내려갈 때")
            @Size(max = 100) String situation,
            @Schema(description = "정도 0(없음)~10(매우 심함)", example = "3")
            @Min(0) @Max(10) Integer level) {
    }
}
