package com.hackathon.domain.survey.controller;

import com.hackathon.domain.survey.dto.SurveyRequest;
import com.hackathon.domain.survey.dto.SurveyResponse;
import com.hackathon.domain.survey.service.SurveyService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Survey", description = "사용자 문진. 목표와 불편함을 느끼는 부위를 받아 AI 설명에 함께 쓴다")
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "문진 저장", description = "다시 보내면 새 문진이 최신이 된다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SurveyResponse> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody SurveyRequest.Create req) {
        return ApiResponse.ok(surveyService.create(userId, req));
    }

    @Operation(summary = "내 최신 문진", description = "문진을 한 적이 없으면 SURVEY404")
    @GetMapping("/latest")
    public ApiResponse<SurveyResponse> findLatest(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(surveyService.findLatest(userId));
    }
}
