package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.Csat;
import com.itsm.srm.domain.repository.CsatRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsatJpaRepository extends JpaRepository<Csat, Long>, CsatRepository {
}
