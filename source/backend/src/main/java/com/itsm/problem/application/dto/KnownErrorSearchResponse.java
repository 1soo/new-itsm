package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "KEDB 검색 결과 항목")
public record KnownErrorSearchResponse(
        Long id,
        String title,
        String rootCause,
        String workaround,
        String problemKey
) {
}
