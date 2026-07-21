package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인 프로세스 적용 상태(targetState) 후보")
public record TargetStateOption(
        @Schema(description = "target_state로 저장될 상태 코드") String value,
        @Schema(description = "표시명") String label
) {
}
