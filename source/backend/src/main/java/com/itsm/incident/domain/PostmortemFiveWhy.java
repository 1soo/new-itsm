package com.itsm.incident.domain;

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
 * 5 Whys 단계.
 */
@Getter
@Entity
@Table(name = "postmortem_five_why",
        uniqueConstraints = @UniqueConstraint(columnNames = {"postmortem_id", "step_no"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostmortemFiveWhy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "postmortem_id", nullable = false)
    private Long postmortemId;

    @Column(name = "step_no", nullable = false)
    private short stepNo;

    @Column(nullable = false, length = 500)
    private String content;

    public PostmortemFiveWhy(Long postmortemId, short stepNo, String content) {
        this.postmortemId = postmortemId;
        this.stepNo = stepNo;
        this.content = content;
    }
}
