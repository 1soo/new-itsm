package com.itsm.change.infrastructure.persistence;

import com.itsm.change.domain.ChangeAffectedSystem;
import com.itsm.change.domain.repository.ChangeAffectedSystemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeAffectedSystemJpaRepository
        extends JpaRepository<ChangeAffectedSystem, Long>, ChangeAffectedSystemRepository {
}
