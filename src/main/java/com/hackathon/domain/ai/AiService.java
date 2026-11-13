package com.hackathon.domain.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.BetaBase64ImageSource;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaJsonOutputFormat;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import java.time.Duration;
import java.util.ArrayList;
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
    private final BetaOutputConfig.Effort effort;
    private final ObjectMapper objectMapper;

    public AiService(ObjectMapper objectMapper,
                     @Value("${anthropic.api-key:}") String apiKey,
                     @Value("${anthropic.model}") String model,
                     @Value("${anthropic.effort}") String effort) {
        // 데모 중 무한 대기보다 빠른 실패가 낫다: 기본 10분 타임아웃을 90초로
        this.client = apiKey.isBlank() ? null : AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(90))
                .maxRetries(1)
                .build();
        this.model = model;
        this.effort = BetaOutputConfig.Effort.of(effort);
        this.objectMapper = objectMapper;
        if (client == null) {
            log.warn("ANTHROPIC_API_KEY 미설정: /api/ai/* 는 503을 반환합니다.");
        }
    }

    public AiDto.ChatResponse chat(AiDto.ChatRequest req) {
        MessageCreateParams.Builder params = baseParams(req.system(), req.history(), req.message(), req.images());
        params.outputConfig(BetaOutputConfig.builder().effort(effort).build());
        return new AiDto.ChatResponse(call(params));
    }

    /** schema에 맞는 JSON만 반환하도록 강제 (structured outputs). */
    public JsonNode json(AiDto.JsonRequest req) {
        MessageCreateParams.Builder params = baseParams(req.system(), req.history(), req.message(), req.images());
        Map<String, Object> schema = JsonSchemas.strict(req.schema());
        BetaJsonOutputFormat.Schema.Builder schemaBuilder = BetaJsonOutputFormat.Schema.builder();
        schema.forEach((k, v) -> schemaBuilder.putAdditionalProperty(k, JsonValue.from(v)));
        params.outputConfig(BetaOutputConfig.builder()
                .effort(effort)
                .format(BetaJsonOutputFormat.builder().schema(schemaBuilder.build()).build())
                .build());

        String text = call(params);
        try {
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            // 응답 원문에 사용자 건강 정보가 섞일 수 있어 길이만 남긴다
            // Jackson 에러 메시지도 토큰 원문을 담을 수 있어 위치 정보만 남긴다
            log.error("AI JSON 파싱 실패: {} (응답 길이 {}자, line {}, column {})",
                    e.getClass().getSimpleName(), text.length(),
                    e.getLocation() == null ? -1 : e.getLocation().getLineNr(),
                    e.getLocation() == null ? -1 : e.getLocation().getColumnNr());
            throw new BusinessException(ErrorCode.AI_ERROR);
        }
    }

    MessageCreateParams.Builder baseParams(String system, List<AiDto.Turn> history, String message,
                                           List<AiDto.Image> images) {
        if (client == null) {
            throw new BusinessException(ErrorCode.AI_NOT_CONFIGURED);
        }
        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(16000L)
                // 안전 분류기가 거절하면 서버가 권장 대체 모델로 같은 요청을 다시 실행한다
                .addBeta(AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01)
                .fallbacksDefault();
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
        if (images == null || images.isEmpty()) {
            params.addUserMessage(message);
        } else {
            List<BetaContentBlockParam> blocks = new ArrayList<>();
            for (AiDto.Image image : images) {
                blocks.add(BetaContentBlockParam.ofImage(BetaBase64ImageSource.builder()
                        .mediaType(BetaBase64ImageSource.MediaType.of(image.mediaType()))
                        .data(rawBase64(image.data()))
                        .build()));
            }
            blocks.add(BetaContentBlockParam.ofText(message));
            params.addUserMessageOfContentBlockParams(blocks);
        }
        return params;
    }

    /** 프론트가 FileReader 결과(data:image/png;base64,....)를 그대로 보내도 받도록 접두어를 뗀다. */
    private static String rawBase64(String data) {
        int comma = data.startsWith("data:") ? data.indexOf(',') : -1;
        return comma < 0 ? data : data.substring(comma + 1);
    }

    private String call(MessageCreateParams.Builder params) {
        BetaMessage response;
        try {
            response = client.beta().messages().create(params.build());
        } catch (RateLimitException e) {
            throw new BusinessException(ErrorCode.AI_RATE_LIMITED);
        } catch (AnthropicException e) {
            log.error("Claude API 호출 실패", e);
            throw new BusinessException(ErrorCode.AI_ERROR);
        }

        if (response.stopReason().filter(BetaStopReason.REFUSAL::equals).isPresent()) {
            throw new BusinessException(ErrorCode.AI_REFUSED);
        }
        if (response.content().stream().anyMatch(block -> block.fallback().isPresent())) {
            log.info("AI 거절 fallback 발생: 응답 모델 {}", response.model());
        }

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining());
    }
}
