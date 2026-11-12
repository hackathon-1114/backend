package com.hackathon.domain.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiService {

    private final AnthropicClient client; // 키 없으면 null
    private final String model;
    private final OutputConfig.Effort effort;
    private final ObjectMapper objectMapper;

    public AiService(ObjectMapper objectMapper,
                     @Value("${anthropic.api-key:}") String apiKey,
                     @Value("${anthropic.model}") String model,
                     @Value("${anthropic.effort}") String effort) {
        this.client = apiKey.isBlank() ? null : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.model = model;
        this.effort = OutputConfig.Effort.of(effort);
        this.objectMapper = objectMapper;
        if (client == null) {
            log.warn("ANTHROPIC_API_KEY 미설정: /api/ai/* 는 503을 반환합니다.");
        }
    }

    public AiDto.ChatResponse chat(AiDto.ChatRequest req) {
        MessageCreateParams.Builder params = baseParams(req.system(), req.history(), req.message());
        params.outputConfig(OutputConfig.builder().effort(effort).build());
        return new AiDto.ChatResponse(call(params));
    }

    /** schema에 맞는 JSON만 반환하도록 강제 (structured outputs). */
    public JsonNode json(AiDto.JsonRequest req) {
        MessageCreateParams.Builder params = baseParams(req.system(), req.history(), req.message());
        Map<String, Object> schema = JsonSchemas.strict(req.schema());
        JsonOutputFormat.Schema.Builder schemaBuilder = JsonOutputFormat.Schema.builder();
        schema.forEach((k, v) -> schemaBuilder.putAdditionalProperty(k, JsonValue.from(v)));
        params.outputConfig(OutputConfig.builder()
                .effort(effort)
                .format(JsonOutputFormat.builder().schema(schemaBuilder.build()).build())
                .build());

        String text = call(params);
        try {
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            // 응답 원문에 사용자 건강 정보가 섞일 수 있어 길이만 남긴다
            log.error("AI JSON 파싱 실패 (응답 길이 {}자): {}", text.length(), e.getOriginalMessage());
            throw new BusinessException(ErrorCode.AI_ERROR);
        }
    }

    private MessageCreateParams.Builder baseParams(String system, List<AiDto.Turn> history, String message) {
        if (client == null) {
            throw new BusinessException(ErrorCode.AI_NOT_CONFIGURED);
        }
        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(16000L);
        if (system != null && !system.isBlank()) {
            params.system(system);
        }
        if (history != null) {
            for (AiDto.Turn t : history) {
                if ("assistant".equals(t.role())) {
                    params.addAssistantMessage(t.content());
                } else {
                    params.addUserMessage(t.content());
                }
            }
        }
        params.addUserMessage(message);
        return params;
    }

    private String call(MessageCreateParams.Builder params) {
        Message response;
        try {
            response = client.messages().create(params.build());
        } catch (RateLimitException e) {
            throw new BusinessException(ErrorCode.AI_RATE_LIMITED);
        } catch (AnthropicServiceException | AnthropicIoException e) {
            log.error("Claude API 호출 실패", e);
            throw new BusinessException(ErrorCode.AI_ERROR);
        }

        if (response.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) {
            throw new BusinessException(ErrorCode.AI_REFUSED);
        }

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining());
    }
}
