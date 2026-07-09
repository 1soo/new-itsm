package com.itsm.asset.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 유형별 속성(EAV).
 */
@Getter
@Entity
@Table(name = "asset_attribute", uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "attr_key"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "attr_key", nullable = false, length = 50)
    private String attrKey;

    @Column(name = "attr_value", length = 500)
    private String attrValue;

    public AssetAttribute(Long assetId, String attrKey, String attrValue) {
        this.assetId = assetId;
        this.attrKey = attrKey;
        this.attrValue = attrValue;
    }
}
