package com.itsm.incident.domain;

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

import java.time.LocalDate;

/**
 * 포스트모템 조치항목.
 */
@Getter
@Entity
@Table(name = "postmortem_action_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostmortemActionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "postmortem_id", nullable = false)
    private Long postmortemId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String owner;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ActionItemStatus status;

    public PostmortemActionItem(Long postmortemId, String description, String owner,
                                LocalDate dueDate, ActionItemStatus status) {
        this.postmortemId = postmortemId;
        this.description = description;
        this.owner = owner;
        this.dueDate = dueDate;
        this.status = status == null ? ActionItemStatus.OPEN : status;
    }
}
