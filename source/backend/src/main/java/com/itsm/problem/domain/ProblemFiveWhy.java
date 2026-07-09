package com.itsm.problem.domain;

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
 * RCA 5 Whys 단계. 문제 1:N.
 */
@Getter
@Entity
@Table(name = "problem_five_why")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemFiveWhy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "step_no", nullable = false)
    private short stepNo;

    @Column(nullable = false, length = 500)
    private String content;

    public ProblemFiveWhy(Long problemId, short stepNo, String content) {
        this.problemId = problemId;
        this.stepNo = stepNo;
        this.content = content;
    }
}
