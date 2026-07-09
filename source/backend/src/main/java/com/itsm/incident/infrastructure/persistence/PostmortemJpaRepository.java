package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.Postmortem;
import com.itsm.incident.domain.repository.PostmortemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostmortemJpaRepository extends JpaRepository<Postmortem, Long>, PostmortemRepository {
}
