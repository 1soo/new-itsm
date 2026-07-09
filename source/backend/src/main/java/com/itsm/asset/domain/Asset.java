package com.itsm.asset.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * IT 자산(HW/SW/클라우드). 생애주기 상태·만료일(라이선스/보증/계약)을 관리한다.
 */
@Getter
@Entity
@Table(name = "asset")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_key", nullable = false, unique = true, length = 20)
    private String assetKey;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AssetType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AssetStatus status;

    @Column(length = 100)
    private String owner;

    @Column(length = 150)
    private String location;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "license_expiry")
    private LocalDate licenseExpiry;

    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;

    @Column(name = "contract_expiry")
    private LocalDate contractExpiry;

    public Asset(String assetKey, String name, AssetType type, String owner, String location,
                 LocalDate purchaseDate, BigDecimal cost,
                 LocalDate licenseExpiry, LocalDate warrantyExpiry, LocalDate contractExpiry) {
        this.assetKey = assetKey;
        this.name = name;
        this.type = type;
        this.status = AssetStatus.PLANNING;
        this.owner = owner;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.cost = cost;
        this.licenseExpiry = licenseExpiry;
        this.warrantyExpiry = warrantyExpiry;
        this.contractExpiry = contractExpiry;
    }

    public void updateContent(String name, String owner, String location, LocalDate purchaseDate, BigDecimal cost,
                              LocalDate licenseExpiry, LocalDate warrantyExpiry, LocalDate contractExpiry) {
        if (name != null) {
            this.name = name;
        }
        if (owner != null) {
            this.owner = owner;
        }
        if (location != null) {
            this.location = location;
        }
        if (purchaseDate != null) {
            this.purchaseDate = purchaseDate;
        }
        if (cost != null) {
            this.cost = cost;
        }
        if (licenseExpiry != null) {
            this.licenseExpiry = licenseExpiry;
        }
        if (warrantyExpiry != null) {
            this.warrantyExpiry = warrantyExpiry;
        }
        if (contractExpiry != null) {
            this.contractExpiry = contractExpiry;
        }
    }

    public void changeStatus(AssetStatus status) {
        this.status = status;
    }

    public LocalDate earliestExpiry() {
        return java.util.stream.Stream.of(licenseExpiry, warrantyExpiry, contractExpiry)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }
}
