package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface EsmChecklistJpaRepository extends JpaRepository<EsmChecklist, Long>, EsmChecklistRepository {

    @Override
    List<EsmChecklist> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
