package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역할 목록 항목")
public record RoleResponse(
        Long id,
        @Schema(description = "역할 코드(user.roles·role 필터와 동일 체계)", example = "SYSTEM_ADMIN") String roleCode,
        @Schema(description = "표시명", example = "시스템 관리자") String name,
        String description,
        long userCount
) {
}
