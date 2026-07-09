package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "코멘트 등록 요청")
public record CommentCreateRequest(
        @NotBlank String body
) {
}
