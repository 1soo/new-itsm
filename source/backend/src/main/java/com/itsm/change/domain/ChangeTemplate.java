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
 * 표준 변경 사전승인 템플릿.
 */
@Getter
@Entity
@Table(name = "change_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    public ChangeTemplate(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
