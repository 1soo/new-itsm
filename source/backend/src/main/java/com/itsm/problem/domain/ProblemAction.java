package com.itsm.problem.domain;

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
 * 후속(시정) 조치. 문제 1:N.
 */
@Getter
@Entity
@Table(name = "problem_action")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String owner;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ActionStatus status;

    public ProblemAction(Long problemId, String description, String owner, LocalDate dueDate) {
        this.problemId = problemId;
        this.description = description;
        this.owner = owner;
        this.dueDate = dueDate;
        this.status = ActionStatus.IN_PROGRESS;
    }

    public void changeStatus(ActionStatus status) {
        this.status = status;
    }
}
