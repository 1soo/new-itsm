package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "자산 수정 요청(부분 갱신, 미지정 필드는 유지)")
public record UpdateAssetRequest(
        String name,
        String owner,
        String location,
        LocalDate purchaseDate,
        BigDecimal cost,
        LocalDate licenseExpiry,
        LocalDate warrantyExpiry,
        LocalDate contractExpiry,
        @Schema(description = "지정 시 속성 전체 교체") Map<String, String> attributes
) {
}
