package com.itsm.esm.infrastructure.persistence;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTaskStatus;
import com.itsm.esm.domain.EsmChecklistTask;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EsmChecklistTaskJpaRepository extends JpaRepository<EsmChecklistTask, Long>, EsmChecklistTaskRepository {

    @Override
    List<EsmChecklistTask> findByChecklistId(Long checklistId);

    @Override
    long countByChecklistIdAndStatusNot(Long checklistId, ChecklistTaskStatus status);

    @Override
    @Query("""
            select t from EsmChecklistTask t
            where t.isDeleted = false
              and (:department is null or t.department = :department)
              and (:status is null or t.status = :status)
            """)
    Page<EsmChecklistTask> search(@Param("department") Department department,
                                  @Param("status") ChecklistTaskStatus status,
                                  Pageable pageable);
}
