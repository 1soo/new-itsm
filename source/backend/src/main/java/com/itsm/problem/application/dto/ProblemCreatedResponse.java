package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 등록 응답")
public record ProblemCreatedResponse(
        Long id,
        String ticketKey,
        String status,
        @Schema(description = "산정 우선순위(영향도·긴급도 중 하나라도 없으면 null)") String priority
) {
}
