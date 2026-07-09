package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "RCA 작성/수정 요청. 개인(사람)을 근본 원인으로 강제하지 않는다.")
public record RcaRequest(
        String rootCause,
        List<String> fiveWhys,
        String category
) {
}
