package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "동적 양식 필드")
public record FormFieldDto(
        String key,
        String label,
        @Schema(description = "text|select|number|date|file") String type,
        boolean required,
        List<String> options
) {
}
