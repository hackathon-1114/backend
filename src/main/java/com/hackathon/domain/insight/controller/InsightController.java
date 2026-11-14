package com.hackathon.domain.insight.controller;

import com.hackathon.domain.insight.dto.InsightResponse;
import com.hackathon.domain.insight.service.InsightService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Insight", description = "운동 기록에서 AI가 찾아낸 패턴")
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @Operation(summary = "패턴 분석 실행",
            description = "X-User-Id 유저의 운동 기록을 AI가 분석해 패턴 3개를 돌려준다. 3~10초 걸리니 로딩 화면 필요. "
                    + "기록이 5개 미만이면 INSIGHT400. 다시 실행하면 이전 결과를 새 결과로 바꾼다")
    @PostMapping("/analyze")
    public ApiResponse<List<InsightResponse>> analyze(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(insightService.analyze(userId));
    }

    @Operation(summary = "마지막 분석 결과 조회", description = "AI를 다시 부르지 않고 저장된 결과만 돌려준다. 분석 전이면 빈 배열")
    @GetMapping
    public ApiResponse<List<InsightResponse>> findMine(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(insightService.findMine(userId));
    }
}
