package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "코멘트")
public record CommentResponse(
        Long id,
        String author,
        String body,
        OffsetDateTime createdAt
) {
}
