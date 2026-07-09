package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "저장된 RCA")
public record RcaResponse(
        String rootCause,
        List<String> fiveWhys,
        String category
) {
}
