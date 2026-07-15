package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "담당자 배정 후보(카탈로그 항목 지정 역할 보유자)")
public record AssigneeCandidateResponse(
        Long id,
        String name
) {
}
