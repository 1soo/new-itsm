package com.itsm.esm.domain.repository;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTaskStatus;
import com.itsm.esm.domain.EsmChecklistTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 체크리스트 하위 작업 저장소 포트.
 */
public interface EsmChecklistTaskRepository {

    EsmChecklistTask save(EsmChecklistTask task);

    Optional<EsmChecklistTask> findById(Long id);

    List<EsmChecklistTask> findByChecklistId(Long checklistId);

    long countByChecklistIdAndStatusNot(Long checklistId, ChecklistTaskStatus status);

    /** 내 하위 작업 목록(scope=mine). 로그인 사용자 department로 강제 필터링. */
    Page<EsmChecklistTask> search(Department department, ChecklistTaskStatus status, Pageable pageable);
}
