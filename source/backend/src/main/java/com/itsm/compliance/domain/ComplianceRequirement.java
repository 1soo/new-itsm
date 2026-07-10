package com.itsm.compliance.domain;

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
 * 컴플라이언스 요구사항. 책임자(owner)·시정조치·변경 연계를 관리한다.
 */
@Getter
@Entity
@Table(name = "compliance_requirement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplianceRequirement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_key", nullable = false, unique = true, length = 20)
    private String requirementKey;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String basis;

    @Column(length = 500)
    private String scope;

    @Column(name = "owner_id")
    private Long ownerId;

    public ComplianceRequirement(String requirementKey, String name, String basis, String scope) {
        this.requirementKey = requirementKey;
        this.name = name;
        this.basis = basis;
        this.scope = scope;
    }

    public void updateDetails(String name, String basis, String scope) {
        this.name = name;
        this.basis = basis;
        this.scope = scope;
    }

    public void assignOwner(Long ownerId) {
        this.ownerId = ownerId;
    }
}
