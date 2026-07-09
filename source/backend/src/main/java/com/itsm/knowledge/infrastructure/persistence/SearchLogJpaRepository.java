package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.SearchLog;
import com.itsm.knowledge.domain.repository.SearchLogRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface SearchLogJpaRepository extends JpaRepository<SearchLog, Long>, SearchLogRepository {

    @Override
    long countBySearchedAtBetween(OffsetDateTime from, OffsetDateTime to);

    @Override
    long countByResultCountAndSearchedAtBetween(int resultCount, OffsetDateTime from, OffsetDateTime to);

    @Override
    List<SearchLog> findByResultCountAndSearchedAtBetween(int resultCount, OffsetDateTime from, OffsetDateTime to);
}
