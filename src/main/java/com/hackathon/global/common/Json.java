package com.hackathon.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.global.error.BusinessException;
import com.hackathon.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 엔티티의 TEXT 컬럼에 목록을 JSON으로 넣고 빼는 도우미. 원문에는 사용자 건강 정보가 섞일 수 있어 실패해도 내용을 로그에 남기지 않는다. */
@Component
@RequiredArgsConstructor
public class Json {

    private final ObjectMapper objectMapper;

    public String write(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public <T> T read(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public <T> T convert(Object tree, TypeReference<T> type) {
        return objectMapper.convertValue(tree, type);
    }
}
