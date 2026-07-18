package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 서비스 요청 저장소 포트.
 */
public interface ServiceRequestRepository {

    ServiceRequest save(ServiceRequest request);

    Optional<ServiceRequest> findById(Long id);

    Page<ServiceRequest> search(Long requesterId, Long categoryId, boolean uncategorized, RequestStatus status,
                                OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /** 통합 검색(API-SEARCH-001) 전용 키워드 검색. ticketKey 또는 카탈로그 항목명 매칭. */
    Page<ServiceRequest> searchByKeyword(Long requesterId, String keyword, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);

    List<ServiceRequest> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    /** 카테고리(카탈로그 항목의 category_id 실시간 조인)의 미종료(CLOSED 제외) 요청 건수. */
    long countOpenByCategoryId(Long categoryId);

    /** 미분류(카탈로그 항목의 category_id IS NULL) 미종료 요청 건수. */
    long countOpenUncategorized();
}
