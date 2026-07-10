package com.itsm.common.ticket;

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
 * 티켓/자산/지식 간 다형(polymorphic) 링크. DB FK 대신 애플리케이션에서 대상 존재를 검증한다.
 */
@Getter
@Entity
@Table(name = "ticket_link",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_type", "source_id", "target_type", "target_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 25)
    private TicketType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TicketType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "link_type", length = 30)
    private String linkType;

    public TicketLink(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId, String linkType) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.linkType = linkType;
    }
}
