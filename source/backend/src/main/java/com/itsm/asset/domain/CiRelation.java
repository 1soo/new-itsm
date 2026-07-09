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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CI 간 자기참조 의존 관계(CMDB). 영향 범위 조회 시 그래프 탐색.
 */
@Getter
@Entity
@Table(name = "ci_relation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_ci_id", "target_ci_id", "relation_type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CiRelation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_ci_id", nullable = false)
    private Long sourceCiId;

    @Column(name = "target_ci_id", nullable = false)
    private Long targetCiId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 20)
    private RelationType relationType;

    public CiRelation(Long sourceCiId, Long targetCiId, RelationType relationType) {
        this.sourceCiId = sourceCiId;
        this.targetCiId = targetCiId;
        this.relationType = relationType;
    }
}
