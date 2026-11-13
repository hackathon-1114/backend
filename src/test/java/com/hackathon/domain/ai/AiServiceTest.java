package com.hackathon.domain.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 실제 호출 없이, Claude API로 나갈 요청이 의도한 모양인지만 확인한다. */
class AiServiceTest {

    private final AiService service = new AiService(new ObjectMapper(), "test-key", "claude-opus-5-5", "low");

    @Test
    void everyRequestOptsIntoRefusalFallback() {
        MessageCreateParams params = service.baseParams(null, null, "안녕", null).build();

        assertThat(params.fallbacks()).hasValueSatisfying(f -> assertThat(f.isDefault()).isTrue());
        assertThat(params.betas().orElseThrow()).contains(AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01);
    }

    @Test
    void imagesGoBeforeTextAndDataUrlPrefixIsStripped() {
        MessageCreateParams params = service.baseParams(null,
                List.of(new AiDto.Turn("user", "이전 질문"), new AiDto.Turn("assistant", "이전 답")),
                "이 약 뭐야?",
                List.of(new AiDto.Image("image/png", "data:image/png;base64,AAAA"))).build();

        assertThat(params.messages()).hasSize(3);
        List<BetaContentBlockParam> blocks = params.messages().get(2).content().orElseThrow().asContentBlockParams();
        assertThat(blocks).hasSize(2);
        assertThat(blocks.get(0).asImage().source().asBase64().data()).isEqualTo("AAAA");
        assertThat(blocks.get(1).asText().text()).isEqualTo("이 약 뭐야?");
    }
}
