package com.itsm.change.infrastructure.persistence;

import com.itsm.change.domain.ChangeTemplate;
import com.itsm.change.domain.repository.ChangeTemplateRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChangeTemplateJpaRepository extends JpaRepository<ChangeTemplate, Long>, ChangeTemplateRepository {

    @Override
    @Query("select t from ChangeTemplate t where t.isDeleted = false")
    List<ChangeTemplate> findActive();
}
