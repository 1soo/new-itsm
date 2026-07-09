package com.itsm.change.domain.repository;

import com.itsm.change.domain.ChangeTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 표준 변경 템플릿 저장소 포트.
 */
public interface ChangeTemplateRepository {

    ChangeTemplate save(ChangeTemplate template);

    Optional<ChangeTemplate> findById(Long id);

    List<ChangeTemplate> findActive();
}
