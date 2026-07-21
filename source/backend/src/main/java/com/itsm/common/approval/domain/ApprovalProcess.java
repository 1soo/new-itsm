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
 * 승인 프로세스 정의(규칙 헤더, 전 도메인 공용). domain/targetState/requestSubtypeKey/requesterRole 4축을
 * 각각 독립 지정할 수 있고, 축이 비어있으면(null 또는 역할 매핑 0개) 해당 축의 모든 값에 매칭되는 것으로 간주한다
 * (domain=null은 전체 도메인 적용, 2026-07-15 우선순위 재설계). targetState는 domain에 종속된 축이라
 * domain=null이면 반드시 null이다(requestSubtypeKey와 동일한 종속 규칙, 2026-07-22 상태별 승인자 지정 확장).
 * 런타임에는 (domain, targetState, requestSubtypeKey, 요청자 보유 역할)에 매칭되는 규칙 중 priorityTier가
 * 가장 큰(가장 좁은 범위) 규칙 하나만 적용한다.
 */
@Getter
@Entity
@Table(name = "approval_process")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalProcess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String domain;

    @Column(name = "target_state", length = 30)
    private String targetState;

    @Column(name = "request_subtype_key", length = 50)
    private String requestSubtypeKey;

    @Column(name = "priority_tier", nullable = false)
    private short priorityTier;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    public ApprovalProcess(String domain, String targetState, String requestSubtypeKey, short priorityTier,
                            String name, String description) {
        this.domain = domain;
        this.targetState = targetState;
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
