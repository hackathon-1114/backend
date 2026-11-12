package com.hackathon.domain.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonSchemasTest {

    @Test
    @SuppressWarnings("unchecked")
    void fillsRequiredAndAdditionalPropertiesRecursively() {
        Map<String, Object> in = Map.of("properties", Map.of(
                "risk", Map.of("type", "string"),
                "tips", Map.of("type", "array", "items", Map.of("properties", Map.of("text", Map.of("type", "string"))))));

        Map<String, Object> out = JsonSchemas.strict(in);

        assertThat(out).containsEntry("type", "object").containsEntry("additionalProperties", false);
        assertThat((List<String>) out.get("required")).containsExactlyInAnyOrder("risk", "tips");
        Map<String, Object> item = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) out.get("properties")).get("tips")).get("items");
        assertThat(item).containsEntry("additionalProperties", false).containsEntry("required", List.of("text"));
    }

    @Test
    void keepsUserProvidedRequired() {
        Map<String, Object> out = JsonSchemas.strict(Map.of(
                "properties", Map.of("a", Map.of("type", "string"), "b", Map.of("type", "string")),
                "required", List.of("a")));
        assertThat(out.get("required")).isEqualTo(List.of("a"));
    }
}
