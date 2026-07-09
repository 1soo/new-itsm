package com.itsm.asset.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구성 항목(CI). 자산과 선택적으로 연결된다.
 */
@Getter
@Entity
@Table(name = "configuration_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfigurationItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(name = "asset_id")
    private Long assetId;

    public ConfigurationItem(String name, String type, Long assetId) {
        this.name = name;
        this.type = type;
        this.assetId = assetId;
    }
}
