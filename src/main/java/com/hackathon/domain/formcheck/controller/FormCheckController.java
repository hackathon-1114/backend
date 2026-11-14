package com.hackathon.domain.formcheck.controller;

import com.hackathon.domain.formcheck.dto.FormCheckRequest;
import com.hackathon.domain.formcheck.dto.FormCheckResponse;
import com.hackathon.domain.formcheck.service.FormCheckService;
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

@Tag(name = "FormCheck", description = "운동 영상 자세 점검. 영상에서 뽑은 장면을 AI가 본다")
@RestController
@RequestMapping("/api/form-checks")
@RequiredArgsConstructor
public class FormCheckController {

    private final FormCheckService formCheckService;

    @Operation(summary = "자세 점검 실행",
            description = "영상에서 시간 순서로 뽑은 장면(1~12장)을 보내면 점수, 잘한 점, 지적 사항을 돌려주고 저장한다. "
                    + "10~20초 걸리니 로딩 화면 필요. 장면에 운동하는 사람이 안 보이면 FORMCHECK422. "
                    + "장면 뽑는 코드는 /form-check.html 소스의 extractFrames 참고")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FormCheckResponse> check(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody FormCheckRequest.Create req) {
        return ApiResponse.ok(formCheckService.check(userId, req));
    }

    @Operation(summary = "내 자세 점검 기록", description = "최근 것부터")
    @GetMapping
    public ApiResponse<List<FormCheckResponse>> findMine(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(formCheckService.findMine(userId));
    }

    @Operation(summary = "자세 점검 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<FormCheckResponse> findOne(@PathVariable Long id) {
        return ApiResponse.ok(formCheckService.findOne(id));
    }
}
