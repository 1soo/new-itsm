package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.Queue;
import com.itsm.srm.domain.repository.QueueRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueJpaRepository extends JpaRepository<Queue, Long>, QueueRepository {
}
