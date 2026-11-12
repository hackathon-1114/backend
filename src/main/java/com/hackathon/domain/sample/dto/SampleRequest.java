package com.hackathon.domain.sample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SampleRequest {

    public record Create(
            @NotBlank @Size(max = 100) String title,
            String content) {
    }

    /** null인 필드는 수정 안 함 */
    public record Update(
            @Size(max = 100) String title,
            String content) {
    }
}
