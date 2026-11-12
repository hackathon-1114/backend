package com.hackathon.domain.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
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

    public AiService(@Value("${anthropic.api-key:}") String apiKey,
                     @Value("${anthropic.model}") String model,
                     @Value("${anthropic.effort}") String effort) {
        this.client = apiKey.isBlank() ? null : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.model = model;
        this.effort = OutputConfig.Effort.of(effort);
        if (client == null) {
            log.warn("ANTHROPIC_API_KEY 미설정: /api/ai/* 는 503을 반환합니다.");
        }
    }

    public AiDto.ChatResponse chat(AiDto.ChatRequest req) {
        if (client == null) {
            throw new BusinessException(ErrorCode.AI_NOT_CONFIGURED);
        }

        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(16000L)
                .outputConfig(OutputConfig.builder().effort(effort).build());
        if (req.system() != null && !req.system().isBlank()) {
            params.system(req.system());
        }
        if (req.history() != null) {
            for (AiDto.Turn t : req.history()) {
                if ("assistant".equals(t.role())) {
                    params.addAssistantMessage(t.content());
                } else {
                    params.addUserMessage(t.content());
                }
            }
        }
        params.addUserMessage(req.message());

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

        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining());
        return new AiDto.ChatResponse(text);
    }
}
