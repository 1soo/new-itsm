package com.itsm.common.approval.domain;

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
 * 승인 프로세스 정의(규칙 헤더, 전 도메인 공용). 런타임에는 (domain, requestSubtypeKey, 요청자 보유 역할)에
 * 매칭되는 규칙 중 priorityTier가 가장 큰(가장 좁은 범위) 규칙 하나만 적용한다.
 */
@Getter
@Entity
@Table(name = "approval_process")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalProcess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String domain;

    @Column(name = "request_subtype_key", length = 50)
    private String requestSubtypeKey;

    @Column(name = "priority_tier", nullable = false)
    private short priorityTier;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    public ApprovalProcess(String domain, String requestSubtypeKey, short priorityTier,
                            String name, String description) {
        this.domain = domain;
        this.requestSubtypeKey = requestSubtypeKey;
        this.priorityTier = priorityTier;
        this.name = name;
        this.description = description;
    }

    public void update(String name, String description, short priorityTier) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        this.priorityTier = priorityTier;
    }
}
