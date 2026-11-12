package com.hackathon.domain.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 프론트가 보낸 간단한 JSON Schema를 structured outputs 규칙에 맞게 보정한다.
 * object마다 additionalProperties=false, required=전체 필드를 (없을 때만) 채워 넣는다.
 */
final class JsonSchemas {

    private JsonSchemas() {
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> strict(Map<String, Object> schema) {
        Map<String, Object> out = new LinkedHashMap<>(schema);

        if (out.get("properties") instanceof Map<?, ?> props) {
            Map<String, Object> fixed = new LinkedHashMap<>();
            props.forEach((k, v) -> fixed.put((String) k,
                    v instanceof Map<?, ?> m ? strict((Map<String, Object>) m) : v));
            out.put("properties", fixed);
            out.putIfAbsent("type", "object");
            out.putIfAbsent("required", new ArrayList<>(fixed.keySet()));
            out.putIfAbsent("additionalProperties", false);
        }
        if (out.get("items") instanceof Map<?, ?> items) {
            out.put("items", strict((Map<String, Object>) items));
        }
        for (String key : List.of("anyOf", "allOf")) {
            if (out.get(key) instanceof List<?> list) {
                out.put(key, list.stream()
                        .map(v -> v instanceof Map<?, ?> m ? strict((Map<String, Object>) m) : v)
                        .toList());
            }
        }
        return out;
    }
}
