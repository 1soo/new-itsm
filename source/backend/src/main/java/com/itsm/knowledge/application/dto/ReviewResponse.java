package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검토·게시 승인/반려 응답")
public record ReviewResponse(Long id, String status) {
}
