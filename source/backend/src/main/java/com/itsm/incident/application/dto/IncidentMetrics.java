package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인시던트 시간 지표(미산정 시 null)")
public record IncidentMetrics(
        Integer mttdMinutes,
        Integer mttaMinutes,
        Integer mttrMinutes
) {
}
