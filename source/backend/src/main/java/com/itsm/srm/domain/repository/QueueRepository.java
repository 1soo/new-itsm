package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.Queue;

import java.util.List;
import java.util.Optional;

/**
 * 큐 저장소 포트.
 */
public interface QueueRepository {

    Optional<Queue> findById(Long id);

    Optional<Queue> findFirstByIsDefaultTrue();

    List<Queue> findAll();
}
