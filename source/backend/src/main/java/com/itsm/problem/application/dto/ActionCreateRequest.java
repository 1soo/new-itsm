package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Schema(description = "후속 조치 등록 요청")
public record ActionCreateRequest(
        @Schema(description = "조치 내용(필수)")
        @NotBlank String description,
        String owner,
        @Schema(description = "기한(ISO-8601 date)") LocalDate dueDate
) {
}
