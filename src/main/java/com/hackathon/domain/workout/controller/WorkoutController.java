package com.hackathon.domain.workout.controller;

import com.hackathon.domain.workout.dto.WorkoutRequest;
import com.hackathon.domain.workout.dto.WorkoutResponse;
import com.hackathon.domain.workout.service.WorkoutService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Workout", description = "운동 기록. 유저 1에는 데모용 5주치 기록이 들어 있다")
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "운동 기록 추가", description = "로그인 대신 X-User-Id 헤더 사용 (없으면 1)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkoutResponse> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody WorkoutRequest.Create req) {
        return ApiResponse.ok(workoutService.create(userId, req));
    }

    @Operation(summary = "운동 기록 목록", description = "최근 운동부터. userId 쿼리 주면 해당 유저 것만")
    @GetMapping
    public ApiResponse<List<WorkoutResponse>> findAll(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(workoutService.findAll(userId));
    }

    @Operation(summary = "운동 기록 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<WorkoutResponse> findOne(@PathVariable Long id) {
        return ApiResponse.ok(workoutService.findOne(id));
    }

    @Operation(summary = "운동 기록 수정", description = "보낸 필드만 수정")
    @PatchMapping("/{id}")
    public ApiResponse<WorkoutResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody WorkoutRequest.Update req) {
        return ApiResponse.ok(workoutService.update(id, req));
    }

    @Operation(summary = "운동 기록 삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workoutService.delete(id);
        return ApiResponse.ok();
    }
}
