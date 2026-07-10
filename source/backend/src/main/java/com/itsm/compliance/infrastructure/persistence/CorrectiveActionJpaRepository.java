package com.itsm.compliance.infrastructure.persistence;

import com.itsm.compliance.domain.CorrectiveAction;
import com.itsm.compliance.domain.repository.CorrectiveActionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CorrectiveActionJpaRepository
        extends JpaRepository<CorrectiveAction, Long>, CorrectiveActionRepository {

    @Override
    List<CorrectiveAction> findByRequirementId(Long requirementId);

    @Override
    List<CorrectiveAction> findByRequirementIdIn(Collection<Long> requirementIds);
}
