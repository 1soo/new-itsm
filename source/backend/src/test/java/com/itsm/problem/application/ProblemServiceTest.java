package com.itsm.problem.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.application.ChangeService;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.problem.application.dto.ActionCreateRequest;
import com.itsm.problem.application.dto.ActionStatusRequest;
import com.itsm.problem.application.dto.CloseRequest;
import com.itsm.problem.application.dto.CreateProblemRequest;
import com.itsm.problem.application.dto.KnownErrorCreateRequest;
import com.itsm.problem.application.dto.LinkRequest;
import com.itsm.problem.application.dto.RcaRequest;
import com.itsm.problem.application.dto.StatusTransitionRequest;
import com.itsm.problem.application.dto.WorkaroundRequest;
import com.itsm.problem.domain.ActionStatus;
import com.itsm.problem.domain.Level;
import com.itsm.problem.domain.LinkTargetType;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemAction;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemStatus;
import com.itsm.problem.domain.repository.KnownErrorRepository;
import com.itsm.problem.domain.repository.ProblemActionRepository;
import com.itsm.problem.domain.repository.ProblemFiveWhyRepository;
import com.itsm.problem.domain.repository.ProblemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProblemServiceTest {

    @Mock ProblemRepository problemRepository;
    @Mock ProblemFiveWhyRepository fiveWhyRepository;
    @Mock KnownErrorRepository knownErrorRepository;
    @Mock ProblemActionRepository actionRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock ChangeService changeService;
    @Mock com.itsm.knowledge.domain.repository.KnowledgeArticleRepository knowledgeArticleRepository;
    @Mock AssetService assetService;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;
    @Mock AppUserRepository appUserRepository;

    ProblemService service;

    @BeforeEach
    void setUp() {
        service = new ProblemService(problemRepository, fiveWhyRepository, knownErrorRepository,
                actionRepository, timelineRepository, ticketLinkRepository, incidentRepository, changeService,
                knowledgeArticleRepository, assetService, approvalGateService, approvalRequestRepository,
                appUserRepository);
        when(problemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fiveWhyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(knownErrorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(actionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fiveWhyRepository.findByProblemIdOrderByStepNoAsc(any())).thenReturn(List.of());
        when(actionRepository.findByProblemId(any())).thenReturn(List.of());
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());
        when(problemRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(any(), any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(String... roles) {
        AuthPrincipal principal = new AuthPrincipal(1L, "pm@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private Problem problem(ProblemStatus status) {
        Problem p = new Problem("PRB-2026-0001", "요약", "설명", ProblemOrigin.REACTIVE,
                "사유", Level.HIGH, Level.HIGH, "api");
        if (status != ProblemStatus.DETECTION) {
            p.changeStatus(status);
        }
        return p;
    }

    // ---------- create + priority matrix ----------

    @Test
    void createComputesPriorityFromMatrix() {
        login("PROBLEM_MANAGER");
        var response = service.create(new CreateProblemRequest("결제 반복 실패", "설명",
                ProblemOrigin.REACTIVE, "다건 인시던트", Level.HIGH, Level.HIGH, "payment"));

        assertThat(response.ticketKey()).startsWith("PRB-");
        assertThat(response.status()).isEqualTo("DETECTION");
        assertThat(response.priority()).isEqualTo("P1");
    }

    @Test
    void createWithMissingUrgencyLeavesPriorityNull() {
        login("PROBLEM_MANAGER");
        var response = service.create(new CreateProblemRequest("요약", null,
                ProblemOrigin.PROACTIVE, null, Level.HIGH, null, null));

        assertThat(response.priority()).isNull();
    }

    @Test
    void createForbiddenForNonManager() {
        login("SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> service.create(new CreateProblemRequest("요약", null,
                null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- detail / not found ----------

    @Test
    void detailNotFound() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.PROBLEM_NOT_FOUND));
    }

    // ---------- status transition ----------

    @Test
    void transitionSequential() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.DETECTION)));
        var response = service.transition(1L, new StatusTransitionRequest(ProblemStatus.CLASSIFICATION, "분류"));
        assertThat(response.status()).isEqualTo("CLASSIFICATION");
    }

    @Test
    void transitionOutOfOrderRejected() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.DETECTION)));
        assertThatThrownBy(() -> service.transition(1L,
                new StatusTransitionRequest(ProblemStatus.RESOLVED_CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionToResolvedClosedGatePassSucceeds() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        var response = service.transition(1L, new StatusTransitionRequest(ProblemStatus.RESOLVED_CLOSED, null));

        assertThat(response.status()).isEqualTo("RESOLVED_CLOSED");
    }

    @Test
    void transitionToResolvedClosedGateBlockedPropagates409() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.transition(1L,
                new StatusTransitionRequest(ProblemStatus.RESOLVED_CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- RCA ----------

    @Test
    void saveRcaReplacesFiveWhys() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        service.saveRca(1L, new RcaRequest("설정 누락", List.of("왜1", "왜2", ""), "CONFIG"));
        verify(fiveWhyRepository).deleteByProblemId(1L);
        verify(fiveWhyRepository, times(2)).save(any()); // 빈 문자열 제외 2건
    }

    // ---------- workaround ----------

    @Test
    void workaroundEmptyRejected() {
        login("PROBLEM_MANAGER");
        assertThatThrownBy(() -> service.addWorkaround(1L, new WorkaroundRequest("  ", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.WORKAROUND_CONTENT_REQUIRED));
    }

    @Test
    void workaroundWithArticleCreatesKnowledgeLink() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(knowledgeArticleRepository.findById(7L)).thenReturn(Optional.of(
                new com.itsm.knowledge.domain.KnowledgeArticle("제목", "본문", null, 1L)));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        service.addWorkaround(1L, new WorkaroundRequest("재시작", 7L));
        verify(ticketLinkRepository).save(any());
    }

    @Test
    void workaroundWithNonExistentArticleRejected() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(knowledgeArticleRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.addWorkaround(1L, new WorkaroundRequest("재시작", 7L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    // ---------- known error ----------

    @Test
    void createKnownError() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.KNOWN_ERROR)));
        var response = service.createKnownError(1L, new KnownErrorCreateRequest("DB 커넥션 풀 고갈", "풀 크기", "재시작"));
        assertThat(response.title()).isEqualTo("DB 커넥션 풀 고갈");
    }

    // ---------- link ----------

    @Test
    void linkIncidentSuccess() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        Incident inc = new Incident("INC-2026-0001", "장애", "설명", Severity.SEV1, "svc", "prod");
        when(incidentRepository.findById(5L)).thenReturn(Optional.of(inc));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        var response = service.link(1L, new LinkRequest(LinkTargetType.INCIDENT, 5L, false));
        assertThat(response.targetType()).isEqualTo("INCIDENT");
        verify(ticketLinkRepository, times(2)).save(any()); // 양방향
    }

    @Test
    void linkIncidentNotFound() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        when(incidentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.link(1L, new LinkRequest(LinkTargetType.INCIDENT, 5L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void linkIncidentWithoutTargetRejected() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        assertThatThrownBy(() -> service.link(1L, new LinkRequest(LinkTargetType.INCIDENT, null, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_REQUIRED));
    }

    @Test
    void linkExistingChangeSuccess() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        when(changeService.existsChange(9L)).thenReturn(true);
        when(changeService.ticketKeyOf(9L)).thenReturn("CHG-2026-0001");
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        var response = service.link(1L, new LinkRequest(LinkTargetType.CHANGE, 9L, false));
        assertThat(response.targetType()).isEqualTo("CHANGE");
        assertThat(response.targetId()).isEqualTo(9L);
        verify(ticketLinkRepository, times(2)).save(any()); // 양방향
    }

    @Test
    void linkNewChangeCreatesRfc() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        when(changeService.createLinkedChange(any(), any())).thenReturn(42L);
        when(changeService.ticketKeyOf(42L)).thenReturn("CHG-2026-0042");
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        var response = service.link(1L, new LinkRequest(LinkTargetType.CHANGE, null, true));
        assertThat(response.targetId()).isEqualTo(42L);
    }

    @Test
    void linkChangeNotFound() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.INVESTIGATION)));
        when(changeService.existsChange(9L)).thenReturn(false);
        assertThatThrownBy(() -> service.link(1L, new LinkRequest(LinkTargetType.CHANGE, 9L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    // ---------- action ----------

    @Test
    void changeActionStatusNotFound() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(actionRepository.findById(50L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.changeActionStatus(1L, 50L, new ActionStatusRequest(ActionStatus.DONE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.PROBLEM_ACTION_NOT_FOUND));
    }

    @Test
    void addAction() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        var response = service.addAction(1L, new ActionCreateRequest("항구 조치", "sre", null));
        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }

    // ---------- close ----------

    @Test
    void closeWithoutOpenActions() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(actionRepository.countByProblemIdAndStatusNot(1L, ActionStatus.DONE)).thenReturn(0L);
        var response = service.close(1L, new CloseRequest(false));
        assertThat(response.status()).isEqualTo("RESOLVED_CLOSED");
        assertThat(response.warning()).isNull();
    }

    @Test
    void closeWithOpenActionsWithoutForceReturnsWarningAndDoesNotClose() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(actionRepository.countByProblemIdAndStatusNot(1L, ActionStatus.DONE)).thenReturn(2L);
        var response = service.close(1L, new CloseRequest(false));
        assertThat(response.status()).isEqualTo("WORKAROUND"); // 종료되지 않음
        assertThat(response.warning()).contains("2건");
        verify(problemRepository, never()).save(any());
    }

    @Test
    void closeWithOpenActionsForced() {
        login("PROBLEM_MANAGER");
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem(ProblemStatus.WORKAROUND)));
        when(actionRepository.countByProblemIdAndStatusNot(1L, ActionStatus.DONE)).thenReturn(1L);
        var response = service.close(1L, new CloseRequest(true));
        assertThat(response.status()).isEqualTo("RESOLVED_CLOSED");
        assertThat(response.warning()).contains("강제 종료");
    }
}
