package com.itsm.knowledge.domain;

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
 * 지식 기사 카테고리.
 */
@Getter
@Entity
@Table(name = "knowledge_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public KnowledgeCategory(String name) {
        this.name = name;
    }
}
