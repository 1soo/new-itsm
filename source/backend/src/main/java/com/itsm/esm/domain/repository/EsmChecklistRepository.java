package com.itsm.esm.domain.repository;

import com.itsm.esm.domain.EsmChecklist;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 온보딩/오프보딩 체크리스트 저장소 포트.
 */
public interface EsmChecklistRepository {

    EsmChecklist save(EsmChecklist checklist);

    Optional<EsmChecklist> findById(Long id);

    List<EsmChecklist> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
