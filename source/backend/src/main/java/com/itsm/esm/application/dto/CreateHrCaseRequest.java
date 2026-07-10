package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "HR 케이스 접수 요청")
public record CreateHrCaseRequest(
        @Schema(description = "대상자") String subjectUserName,
        @Schema(description = "제목(필수)")
        @NotBlank String title,
        String description
) {
}
