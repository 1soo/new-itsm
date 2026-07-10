package com.itsm.esm.application;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.application.dto.CreateHrCaseRequest;
import com.itsm.esm.application.dto.HrCaseCreatedResponse;
import com.itsm.esm.application.dto.HrCaseDetailResponse;
import com.itsm.esm.application.dto.HrCaseStatusTransitionRequest;
import com.itsm.esm.application.dto.HrCaseSummaryResponse;
import com.itsm.esm.application.dto.StatusResponse;
import com.itsm.esm.domain.EsmHrCase;
import com.itsm.esm.domain.HrCaseStatus;
import com.itsm.esm.domain.repository.EsmHrCaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * HR 케이스 유스케이스(API-ESM-010~013). 민감 정보라 HR_CASE_MANAGER 전용(그 외 역할은 SYSTEM_ADMIN
 * 포함 전부 403, REQ-ESM-004). 상태 전이는 접수→기록→조사→해결 순서만 허용한다.
 */
@Service
public class EsmHrCaseService {

    private static final String HR_CASE_MANAGER = "HR_CASE_MANAGER";
    private static final TicketType TT = TicketType.HR_CASE;

    private final EsmHrCaseRepository hrCaseRepository;
    private final TimelineEventRepository timelineRepository;

    public EsmHrCaseService(EsmHrCaseRepository hrCaseRepository,
                            TimelineEventRepository timelineRepository) {
        this.hrCaseRepository = hrCaseRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional
    public HrCaseCreatedResponse create(CreateHrCaseRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        requireHrCaseManager(principal);
        EsmHrCase saved = hrCaseRepository.save(
                new EsmHrCase(request.title(), request.description(), request.subjectUserName()));
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "STATUS_INTAKE", "케이스가 접수되었습니다."));
        return new HrCaseCreatedResponse(saved.getId(), saved.getStatus().name());
    }

    @Transactional(readOnly = true)
    public PageResponse<HrCaseSummaryResponse> list(HrCaseStatus status, Pageable pageable) {
        requireHrCaseManager(SecurityUtils.currentPrincipal());
        return PageResponse.from(hrCaseRepository.search(status, pageable), this::toSummary);
    }

    @Transactional(readOnly = true)
    public HrCaseDetailResponse detail(Long id) {
        requireHrCaseManager(SecurityUtils.currentPrincipal());
        EsmHrCase hrCase = findCase(id);
        List<HrCaseDetailResponse.HistoryEntry> history =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new HrCaseDetailResponse.HistoryEntry(
                                t.getEventType().replace("STATUS_", ""), t.getCreatedBy(), t.getOccurredAt()))
                        .toList();
        return new HrCaseDetailResponse(hrCase.getId(), hrCase.getTitle(), hrCase.getDescription(),
                hrCase.getSubjectUserName(), hrCase.getStatus().name(), history);
    }

    @Transactional
    public StatusResponse transition(Long id, HrCaseStatusTransitionRequest request) {
        requireHrCaseManager(SecurityUtils.currentPrincipal());
        EsmHrCase hrCase = findCase(id);
        HrCaseStatus target = request.targetStatus();
        if (!isNextInSequence(hrCase.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        hrCase.changeStatus(target);
        hrCaseRepository.save(hrCase);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                request.note() != null ? request.note() : "상태가 " + target.name() + "로 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    private boolean isNextInSequence(HrCaseStatus current, HrCaseStatus target) {
        HrCaseStatus[] order = HrCaseStatus.values();
        int currentIndex = current.ordinal();
        int targetIndex = target.ordinal();
        return targetIndex == currentIndex + 1 && targetIndex < order.length;
    }

    private void requireHrCaseManager(AuthPrincipal principal) {
        if (!principal.roles().contains(HR_CASE_MANAGER)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private EsmHrCase findCase(Long id) {
        return hrCaseRepository.findById(id)
                .filter(h -> !h.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ESM_HR_CASE_NOT_FOUND));
    }

    private HrCaseSummaryResponse toSummary(EsmHrCase h) {
        return new HrCaseSummaryResponse(h.getId(), h.getTitle(), h.getStatus().name(), h.getUpdatedAt());
    }
}
