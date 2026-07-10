package com.itsm.esm.application;

import com.itsm.auth.domain.Department;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.application.dto.EsmMetricsResponse;
import com.itsm.esm.domain.ChecklistStatus;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ESM 지표 집계(API-ESM-017): 요청 건수·평균 처리 시간(분, 생성~완료 시각차)·온보딩/오프보딩 체크리스트 완료율.
 * 완료=STATUS_COMPLETED 타임라인 이벤트 시각 기준. 데이터 없으면 0.
 */
@Service
public class EsmMetricsService {

    private static final TicketType TT = TicketType.ESM_REQUEST;
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    private final EsmRequestRepository requestRepository;
    private final EsmChecklistRepository checklistRepository;
    private final TimelineEventRepository timelineRepository;

    public EsmMetricsService(EsmRequestRepository requestRepository,
                             EsmChecklistRepository checklistRepository,
                             TimelineEventRepository timelineRepository) {
        this.requestRepository = requestRepository;
        this.checklistRepository = checklistRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional(readOnly = true)
    public EsmMetricsResponse compute(OffsetDateTime from, OffsetDateTime to, Department department) {
        OffsetDateTime fromV = from != null ? from : EPOCH;
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);

        List<EsmRequest> requests = requestRepository.findByCreatedAtBetween(fromV, toV).stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> department == null || r.getDepartment() == department)
                .toList();

        double avgProcessingMinutes = 0;
        if (!requests.isEmpty()) {
            List<Long> ids = requests.stream().map(EsmRequest::getId).toList();
            Map<Long, OffsetDateTime> completedAt = timelineRepository
                    .findByTicketTypeAndEventTypeAndTicketIdIn(TT, "STATUS_COMPLETED", ids).stream()
                    .collect(Collectors.toMap(TimelineEvent::getTicketId, TimelineEvent::getOccurredAt, (a, b) -> a.isBefore(b) ? a : b));
            Map<Long, OffsetDateTime> createdAtById = requests.stream()
                    .collect(Collectors.toMap(EsmRequest::getId, EsmRequest::getCreatedAt));
            avgProcessingMinutes = completedAt.entrySet().stream()
                    .filter(e -> createdAtById.containsKey(e.getKey()))
                    .mapToDouble(e -> Duration.between(createdAtById.get(e.getKey()), e.getValue()).toSeconds() / 60.0)
                    .average()
                    .orElse(0);
        }

        List<EsmChecklist> checklists = checklistRepository.findByCreatedAtBetween(fromV, toV).stream()
                .filter(c -> !c.isDeleted())
                .toList();
        double onboardingRate = completionRate(checklists, ChecklistTemplateType.ONBOARDING);
        double offboardingRate = completionRate(checklists, ChecklistTemplateType.OFFBOARDING);

        return new EsmMetricsResponse(requests.size(), round(avgProcessingMinutes), round(onboardingRate), round(offboardingRate));
    }

    private double completionRate(List<EsmChecklist> checklists, ChecklistTemplateType type) {
        List<EsmChecklist> filtered = checklists.stream().filter(c -> c.getType() == type).toList();
        if (filtered.isEmpty()) {
            return 0;
        }
        long completed = filtered.stream().filter(c -> c.getStatus() == ChecklistStatus.COMPLETED).count();
        return (double) completed / filtered.size() * 100.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
