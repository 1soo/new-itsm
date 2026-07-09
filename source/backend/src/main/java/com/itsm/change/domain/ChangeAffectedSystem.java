package com.itsm.change.domain;

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
 * 변경 영향 시스템. 변경 1:N.
 */
@Getter
@Entity
@Table(name = "change_affected_system")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeAffectedSystem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_id", nullable = false)
    private Long changeId;

    @Column(name = "system_name", nullable = false, length = 150)
    private String systemName;

    public ChangeAffectedSystem(Long changeId, String systemName) {
        this.changeId = changeId;
        this.systemName = systemName;
    }
}
