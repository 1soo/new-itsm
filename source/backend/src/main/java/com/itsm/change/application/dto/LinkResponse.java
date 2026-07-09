package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연계 결과(양방향 링크 생성)")
public record LinkResponse(Long changeId, String targetType, Long targetId) {
}
