package com.itsm.asset.application.dto;

import com.itsm.asset.domain.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "자산 등록 요청")
public record CreateAssetRequest(
        @Schema(description = "이름(필수)")
        @NotBlank String name,
        @Schema(description = "유형(필수) HARDWARE|SOFTWARE|CLOUD")
        @NotNull AssetType type,
        String owner,
        String location,
        LocalDate purchaseDate,
        BigDecimal cost,
        LocalDate licenseExpiry,
        LocalDate warrantyExpiry,
        LocalDate contractExpiry,
        @Schema(description = "유형별 표준 속성") Map<String, String> attributes
) {
}
