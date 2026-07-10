package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmChecklistTemplateTask;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsmChecklistTemplateTaskJpaRepository
        extends JpaRepository<EsmChecklistTemplateTask, Long>, EsmChecklistTemplateTaskRepository {
}
