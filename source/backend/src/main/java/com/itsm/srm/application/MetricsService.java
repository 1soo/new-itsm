package com.itsm.srm.application;

import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.srm.application.dto.MetricsResponse;
import com.itsm.srm.domain.Csat;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.CsatRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 요청 지표 집계(API-SRM-015): CSAT 평균, 평균 응답/해결 시간(분), SLA 준수율(%).
 * 응답=STATUS_VALIDATED 이벤트, 해결=STATUS_FULFILLED 이벤트 시각 기준.
 */
@Service
public class MetricsService {

    private static final TicketType TT = TicketType.SERVICE_REQUEST;

    private final ServiceRequestRepository requestRepository;
    private final CsatRepository csatRepository;
    private final TimelineEventRepository timelineRepository;

    public MetricsService(ServiceRequestRepository requestRepository,
                          CsatRepository csatRepository,
                          TimelineEventRepository timelineRepository) {
        this.requestRepository = requestRepository;
        this.csatRepository = csatRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional(readOnly = true)
    public MetricsResponse compute(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);

        List<ServiceRequest> requests = requestRepository.findByCreatedAtBetween(fromV, toV).stream()
                .filter(r -> !r.isDeleted())
                .toList();
        if (requests.isEmpty()) {
            return new MetricsResponse(0, 0, 0, 0);
        }
        List<Long> ids = requests.stream().map(ServiceRequest::getId).toList();

        List<Csat> csats = csatRepository.findByServiceRequestIdIn(ids);
        double csatAvg = csats.isEmpty() ? 0
                : csats.stream().mapToInt(Csat::getScore).average().orElse(0);

        Map<Long, OffsetDateTime> validatedAt = earliestOccurrence("STATUS_VALIDATED", ids);
        Map<Long, OffsetDateTime> fulfilledAt = earliestOccurrence("STATUS_FULFILLED", ids);
        Map<Long, OffsetDateTime> createdById = requests.stream()
                .collect(Collectors.toMap(ServiceRequest::getId, ServiceRequest::getCreatedAt));

        double avgResponse = averageMinutes(createdById, validatedAt);
        double avgResolve = averageMinutes(createdById, fulfilledAt);

        double slaCompliance = slaComplianceRate(requests, fulfilledAt);

        return new MetricsResponse(round(csatAvg), round(avgResponse), round(avgResolve), round(slaCompliance));
    }

    private Map<Long, OffsetDateTime> earliestOccurrence(String eventType, List<Long> ids) {
        return timelineRepository.findByTicketTypeAndEventTypeAndTicketIdIn(TT, eventType, ids).stream()
                .collect(Collectors.toMap(
                        TimelineEvent::getTicketId,
                        TimelineEvent::getOccurredAt,
                        (a, b) -> a.isBefore(b) ? a : b));
    }

    private double averageMinutes(Map<Long, OffsetDateTime> createdById, Map<Long, OffsetDateTime> eventById) {
        return eventById.entrySet().stream()
                .filter(e -> createdById.containsKey(e.getKey()))
                .mapToDouble(e -> Duration.between(createdById.get(e.getKey()), e.getValue()).toSeconds() / 60.0)
                .average()
                .orElse(0);
    }

    private double slaComplianceRate(List<ServiceRequest> requests, Map<Long, OffsetDateTime> fulfilledAt) {
        List<ServiceRequest> resolved = requests.stream()
                .filter(r -> r.getSlaResolveDue() != null && fulfilledAt.containsKey(r.getId()))
                .toList();
        if (resolved.isEmpty()) {
            return 0;
        }
        long compliant = resolved.stream()
                .filter(r -> !fulfilledAt.get(r.getId()).isAfter(r.getSlaResolveDue()))
                .count();
        return (double) compliant / resolved.size() * 100.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
