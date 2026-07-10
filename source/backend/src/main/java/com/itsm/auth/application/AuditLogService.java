package com.itsm.auth.application;

import com.itsm.auth.application.dto.AuditLogResponse;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AuditLog;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.repository.AuditLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 감사 로그 기록·조회.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 성공/변경 이벤트 기록. 호출자 트랜잭션에 합류하여 해당 작업과 원자적으로 커밋된다.
     * (예: 계정 생성 감사는 신규 user INSERT와 같은 트랜잭션에서 기록되어 FK가 정상 참조됨)
     */
    @Transactional
    public void record(EventType eventType, Long actorId, String actorEmail, String target, AuditResult result) {
        save(eventType, actorId, actorEmail, target, result);
    }

    /**
     * 롤백돼도 보존해야 하는 이벤트 기록(예: 로그인 실패). REQUIRES_NEW로 독립 트랜잭션에서 즉시 커밋한다.
     * 호출자 트랜잭션이 롤백돼도 FAILURE 감사 로그가 남는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSeparately(EventType eventType, Long actorId, String actorEmail, String target, AuditResult result) {
        save(eventType, actorId, actorEmail, target, result);
    }

    private void save(EventType eventType, Long actorId, String actorEmail, String target, AuditResult result) {
        auditLogRepository.save(AuditLog.of(eventType, actorId, actorEmail, target, result));
    }

    /**
     * 다중 이벤트 타입 조회(예: 컴플라이언스 전용 감사 로그, API-COMP-009). 기존 단일 EventType search()는 변경하지 않는다.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findByEventTypes(Collection<EventType> eventTypes, OffsetDateTime from, OffsetDateTime to) {
        return auditLogRepository.findByEventTypeInAndOccurredAtBetweenOrderByOccurredAtDesc(eventTypes, from, to);
    }

    private static final OffsetDateTime MIN_TIME = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(EventType eventType, String actor, String target,
                                                 OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        // from/to는 null이면 넓은 범위로 대체(PostgreSQL null 파라미터 타입 추론 회피).
        OffsetDateTime fromValue = from != null ? from : MIN_TIME;
        OffsetDateTime toValue = to != null ? to : OffsetDateTime.now().plusYears(100);
        return PageResponse.from(
                auditLogRepository.search(eventType, actor, target, fromValue, toValue, pageable),
                a -> new AuditLogResponse(
                        a.getId(),
                        a.getEventType().name(),
                        a.getActorEmail(),
                        a.getTarget(),
                        a.getResult().name(),
                        a.getOccurredAt()));
    }
}
