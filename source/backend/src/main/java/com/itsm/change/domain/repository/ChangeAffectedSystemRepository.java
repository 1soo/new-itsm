package com.itsm.change.domain.repository;

import com.itsm.change.domain.ChangeAffectedSystem;

/**
 * 변경 영향 시스템 저장소 포트.
 */
public interface ChangeAffectedSystemRepository {

    ChangeAffectedSystem save(ChangeAffectedSystem system);
}
