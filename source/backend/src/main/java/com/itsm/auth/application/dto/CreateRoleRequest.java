package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "역할 생성 요청")
public record CreateRoleRequest(
        @Schema(description = "역할 코드(대문자 스네이크, 필수)", example = "ASSET_AUDITOR")
        @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "roleCode는 대문자 스네이크 표기여야 합니다.") String roleCode,
        @Schema(description = "역할명(필수)")
        @NotBlank String name,
        @Schema(description = "설명") String description
) {
}
