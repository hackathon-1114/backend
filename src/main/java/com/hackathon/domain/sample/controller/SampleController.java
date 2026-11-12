package com.hackathon.domain.sample.controller;

import com.hackathon.domain.sample.dto.SampleRequest;
import com.hackathon.domain.sample.dto.SampleResponse;
import com.hackathon.domain.sample.service.SampleService;
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

@Tag(name = "Sample", description = "복붙용 CRUD 템플릿")
@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @Operation(summary = "생성", description = "로그인 대신 X-User-Id 헤더 사용 (없으면 1)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SampleResponse> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody SampleRequest.Create req) {
        return ApiResponse.ok(sampleService.create(userId, req));
    }

    @Operation(summary = "목록", description = "userId 쿼리 주면 해당 유저 것만")
    @GetMapping
    public ApiResponse<List<SampleResponse>> findAll(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(sampleService.findAll(userId));
    }

    @Operation(summary = "단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<SampleResponse> findOne(@PathVariable Long id) {
        return ApiResponse.ok(sampleService.findOne(id));
    }

    @Operation(summary = "수정", description = "보낸 필드만 수정")
    @PatchMapping("/{id}")
    public ApiResponse<SampleResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody SampleRequest.Update req) {
        return ApiResponse.ok(sampleService.update(id, req));
    }

    @Operation(summary = "삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sampleService.delete(id);
        return ApiResponse.ok();
    }
}
