package com.hackathon.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.hackathon.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "Claude API 래퍼")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "채팅", description = "system(선택) + message + history(선택) → reply")
    @PostMapping("/chat")
    public ApiResponse<AiDto.ChatResponse> chat(@Valid @RequestBody AiDto.ChatRequest req) {
        return ApiResponse.ok(aiService.chat(req));
    }

    @Operation(summary = "JSON 응답", description = "schema 모양 그대로의 JSON을 data로 반환")
    @PostMapping("/json")
    public ApiResponse<JsonNode> json(@Valid @RequestBody AiDto.JsonRequest req) {
        return ApiResponse.ok(aiService.json(req));
    }
}
