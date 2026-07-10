package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "체크리스트 상세")
public record ChecklistDetailResponse(
        Long id,
        String type,
        String targetUserName,
        String status,
        List<TaskInfo> tasks
) {
    @Schema(description = "하위 작업")
    public record TaskInfo(Long id, Department department, String description, String status,
                           Long relatedAssetId, String relatedAssetKey) {
    }
}
