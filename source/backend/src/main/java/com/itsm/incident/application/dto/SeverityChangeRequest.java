package com.itsm.incident.application.dto;

import com.itsm.incident.domain.Priority;
import com.itsm.incident.domain.Severity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "심각도·우선순위 변경 요청")
public record SeverityChangeRequest(
        @Schema(description = "SEV1|SEV2|SEV3") Severity severity,
        @Schema(description = "P1|P2|P3|P4") Priority priority
) {
}
