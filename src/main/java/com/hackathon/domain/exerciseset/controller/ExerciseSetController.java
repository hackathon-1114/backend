package com.hackathon.domain.exerciseset.controller;

import com.hackathon.domain.exerciseset.dto.ExerciseSetRequest;
import com.hackathon.domain.exerciseset.dto.ExerciseSetResponse;
import com.hackathon.domain.exerciseset.service.ExerciseSetService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Set", description = "카메라로 추적한 운동 세트. 반복별 수치를 저장하고 세트 안에서 무너진 시점을 찾는다")
@RestController
@RequestMapping("/api/sets")
@RequiredArgsConstructor
public class ExerciseSetController {

    private final ExerciseSetService exerciseSetService;

    @Operation(summary = "세트 저장",
            description = "브라우저가 잰 반복별 수치를 저장하고 통계(평균 깊이, 후반 변화, 무너지기 시작한 회차)를 바로 돌려준다. "
                    + "AI를 쓰지 않아 즉시 끝난다. reps는 /pose-test.html의 window.poseTest.reps 모양 그대로다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExerciseSetResponse> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody ExerciseSetRequest.Create req) {
        return ApiResponse.ok(exerciseSetService.create(userId, req));
    }

    @Operation(summary = "세트 AI 피드백 생성",
            description = "저장된 세트의 수치를 AI가 문장으로 설명한다. 유저의 인바디와 문진이 있으면 함께 본다. "
                    + "3~12초 걸리니 통계를 먼저 보여 주고 이 결과는 나중에 채운다")
    @PostMapping("/{id}/feedback")
    public ApiResponse<ExerciseSetResponse> feedback(@PathVariable Long id) {
        return ApiResponse.ok(exerciseSetService.feedback(id));
    }

    @Operation(summary = "내 세트 기록", description = "최근 것부터")
    @GetMapping
    public ApiResponse<List<ExerciseSetResponse>> findMine(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(exerciseSetService.findMine(userId));
    }

    @Operation(summary = "세트 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<ExerciseSetResponse> findOne(@PathVariable Long id) {
        return ApiResponse.ok(exerciseSetService.findOne(id));
    }
}
