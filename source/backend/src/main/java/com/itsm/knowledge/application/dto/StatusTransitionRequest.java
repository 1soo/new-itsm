package com.itsm.knowledge.application.dto;

import com.itsm.knowledge.domain.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "IN_REVIEW · 검토 요청")
        @NotNull ArticleStatus targetStatus
) {
}
