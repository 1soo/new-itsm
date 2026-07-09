package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.SearchLog;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 검색 로그 저장소 포트.
 */
public interface SearchLogRepository {

    SearchLog save(SearchLog searchLog);

    long countBySearchedAtBetween(OffsetDateTime from, OffsetDateTime to);

    long countByResultCountAndSearchedAtBetween(int resultCount, OffsetDateTime from, OffsetDateTime to);

    List<SearchLog> findByResultCountAndSearchedAtBetween(int resultCount, OffsetDateTime from, OffsetDateTime to);
}
