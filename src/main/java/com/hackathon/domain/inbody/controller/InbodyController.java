package com.hackathon.domain.inbody.controller;

import com.hackathon.domain.inbody.dto.InbodyRequest;
import com.hackathon.domain.inbody.dto.InbodyResponse;
import com.hackathon.domain.inbody.service.InbodyService;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inbody", description = "인바디 결과지. 체성분과 부위별 근육량의 좌우 차이까지만 다룬다 (자세는 판단하지 않음)")
@RestController
@RequestMapping("/api/inbody")
@RequiredArgsConstructor
public class InbodyController {

    private final InbodyService inbodyService;

    @Operation(summary = "인바디 기록 추가",
            description = "sample을 true로 보내면 가상 수치를 저장하고, values를 보내면 그대로 저장하고, image(결과지 사진)를 보내면 AI가 수치를 읽는다. "
                    + "사진이 결과지가 아니거나 수치를 하나도 못 읽으면 INBODY422, 셋 다 없으면 INBODY400. "
                    + "읽은 값이 틀렸으면 고친 values로 다시 보내면 된다")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InbodyResponse> create(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody InbodyRequest.Create req) {
        return ApiResponse.ok(inbodyService.create(userId, req));
    }

    @Operation(summary = "내 인바디 기록", description = "최근 측정부터")
    @GetMapping
    public ApiResponse<List<InbodyResponse>> findMine(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ApiResponse.ok(inbodyService.findMine(userId));
    }
}
