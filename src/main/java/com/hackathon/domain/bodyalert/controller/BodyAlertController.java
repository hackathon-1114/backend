package com.hackathon.domain.bodyalert.controller;

import com.hackathon.domain.bodyalert.dto.BodyAlertResponse;
import com.hackathon.domain.bodyalert.service.BodyAlertService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "BodyAlert", description = "STEP 03. 인바디와 문진만으로 만드는 사전 분석. 카메라로 확인하면 좋을 영역을 알려 준다")
@RestController
@RequestMapping("/api/body-alerts")
@RequiredArgsConstructor
public class BodyAlertController {

    private final BodyAlertService bodyAlertService;

    @Operation(summary = "사전 분석 실행",
            description = "유저의 최신 인바디와 문진을 AI가 함께 보고 확인하면 좋을 영역 2~3개를 돌려준다. 5~12초 걸린다. "
                    + "인바디와 문진이 둘 다 없으면 BODYALERT400")
    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BodyAlertResponse> analyze(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(bodyAlertService.analyze(userId));
    }

    @Operation(summary = "마지막 사전 분석 조회", description = "AI를 다시 부르지 않는다. 분석한 적이 없으면 BODYALERT404")
    @GetMapping("/latest")
    public ApiResponse<BodyAlertResponse> findLatest(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(bodyAlertService.findLatest(userId));
    }
}
