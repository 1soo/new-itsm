package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역할 옵션(비관리자 공개용, 관리자 전용 필드 제외)")
public record RoleOptionResponse(
        Long id,
        @Schema(description = "역할 코드", example = "SYSTEM_ADMIN") String roleCode,
        @Schema(description = "표시명", example = "시스템 관리자") String name
) {
}
