package com.itsm.change.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.application.dto.ChangeApprovalDecision;
import com.itsm.change.application.dto.ChangeApprovalRequest;
import com.itsm.change.application.dto.ClassificationRequest;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.application.dto.LinkRequest;
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.ScheduleItemResponse;
import com.itsm.change.application.dto.StatusTransitionRequest;
import com.itsm.change.domain.ApprovalRoute;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.LinkTargetType;
import com.itsm.change.domain.Outcome;
import com.itsm.change.domain.repository.ChangeAffectedSystemRepository;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.change.domain.repository.ChangeTemplateRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.ApprovalStatus;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.ApprovalRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemOrigin;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeServiceTest {

    @Mock ChangeRequestRepository changeRequestRepository;
    @Mock ChangeTemplateRepository templateRepository;
    @Mock ChangeAffectedSystemRepository affectedSystemRepository;
    @Mock ApprovalRepository approvalRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock ProblemRepository problemRepository;
    @Mock AppUserRepository appUserRepository;

    ChangeService service;

    @BeforeEach
    void setUp() {
        service = new ChangeService(changeRequestRepository, templateRepository, affectedSystemRepository,
                approvalRepository, ticketLinkRepository, timelineRepository, incidentRepository,
                problemRepository, appUserRepository);
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(String... roles) {
        AuthPrincipal principal = new AuthPrincipal(1L, "cm@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private ChangeRequest change(ChangeStatus status, ApprovalRoute route) {
        ChangeRequest c = new ChangeRequest("CHG-2026-0001", "요약", "설명", ChangeType.NORMAL, ChangeRisk.HIGH,
                route, "구현계획", "롤백계획", null, null);
        if (status != ChangeStatus.REQUESTED) {
            c.changeStatus(status);
        }
        return c;
    }

    // ---------- create + approval route classification ----------

    @Test
    void createUnassessedRiskDefaultsToCab() {
        login("CHANGE_MANAGER");
        var response = service.create(new CreateChangeRequest("요약", "설명", ChangeType.NORMAL, null,
                "계획", List.of("sys-a"), "롤백", null, null));
        assertThat(response.ticketKey()).startsWith("CHG-");
        assertThat(response.status()).isEqualTo("REQUESTED");
        verify(affectedSystemRepository).save(any());
    }

    @Test
    void createHighRiskDefaultsToCab() {
        login("CHANGE_MANAGER");
        service.create(new CreateChangeRequest("요약", null, ChangeType.NORMAL, ChangeRisk.HIGH,
                null, null, null, null, null));
        // 저장된 change의 approvalRoute 확인은 classify 응답으로 별도 검증(against 별도 테스트)
    }

    @Test
    void createStandardWithTemplateIsAuto() {
        login("CHANGE_MANAGER");
        when(templateRepository.findById(1L)).thenReturn(Optional.of(new com.itsm.change.domain.ChangeTemplate("표준 패치", "설명")));
        var response = service.create(new CreateChangeRequest("표준 배포", null, ChangeType.STANDARD, ChangeRisk.LOW,
                null, null, null, null, 1L));
        assertThat(response.type()).isEqualTo("STANDARD");

        var captor = org.mockito.ArgumentCaptor.forClass(ChangeRequest.class);
        verify(changeRequestRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getApprovalRoute()).isEqualTo(ApprovalRoute.AUTO);
    }

    @Test
    void createStandardWithTemplateIsAutoEvenWhenRiskUnassessed() {
        // TC-CHG-015: 표준 변경(templateId 지정)은 위험도 미평가 시에도 AUTO여야 한다(risk 기본 CAB 규칙보다 우선).
        login("CHANGE_MANAGER");
        when(templateRepository.findById(1L)).thenReturn(Optional.of(new com.itsm.change.domain.ChangeTemplate("표준 패치", "설명")));
        service.create(new CreateChangeRequest("표준 배포", null, ChangeType.STANDARD, null,
                null, null, null, null, 1L));

        var captor = org.mockito.ArgumentCaptor.forClass(ChangeRequest.class);
        verify(changeRequestRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getApprovalRoute()).isEqualTo(ApprovalRoute.AUTO);
    }

    @Test
    void createInvalidTemplateRejected() {
        login("CHANGE_MANAGER");
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(new CreateChangeRequest("요약", null, ChangeType.STANDARD,
                ChangeRisk.LOW, null, null, null, null, 99L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createForbiddenForNonManager() {
        login("SERVICE_DESK_AGENT");
        assertThatThrownBy(() -> service.create(new CreateChangeRequest("요약", null, ChangeType.NORMAL,
                null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- detail ----------

    @Test
    void detailNotFound() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.CHANGE_NOT_FOUND));
    }

    @Test
    void detailAllowedForApprover() {
        login("APPROVER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());
        var detail = service.detail(1L);
        assertThat(detail.status()).isEqualTo("REQUESTED");
    }

    // ---------- status transition ----------

    @Test
    void transitionSequential() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        var response = service.transition(1L, new StatusTransitionRequest(ChangeStatus.REVIEW, "검토"));
        assertThat(response.status()).isEqualTo("REVIEW");
    }

    @Test
    void transitionOutOfOrderRejected() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionIntoApprovalCreatesPendingApprovalForCabRoute() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.PLANNING, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.empty());
        service.transition(1L, new StatusTransitionRequest(ChangeStatus.APPROVAL, null));
        verify(approvalRepository).save(any());
    }

    @Test
    void transitionToImplementationBeforeApprovalRejected() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING));
    }

    @Test
    void transitionToImplementationAutoRouteSkipsApproval() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.AUTO)));
        var response = service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(response.status()).isEqualTo("IMPLEMENTATION");
    }

    @Test
    void transitionToImplementationApprovedSucceeds() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        Approval approved = new Approval(TicketType.CHANGE, 1L, "APPROVER");
        approved.approve(2L, "승인");
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.of(approved));
        var response = service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(response.status()).isEqualTo("IMPLEMENTATION");
    }

    // ---------- classification ----------

    @Test
    void classifyUpdatesApprovalRoute() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        var response = service.classify(1L, new ClassificationRequest(ChangeType.NORMAL, ChangeRisk.MEDIUM));
        assertThat(response.approvalRoute()).isEqualTo("PEER_REVIEW");
    }

    // ---------- approval decision ----------

    @Test
    void decideApprovalApproveSuccess() {
        login("APPROVER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L))
                .thenReturn(Optional.of(new Approval(TicketType.CHANGE, 1L, "APPROVER")));
        var response = service.decideApproval(1L, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, "승인합니다"));
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void decideApprovalRejectRequiresReason() {
        login("APPROVER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L))
                .thenReturn(Optional.of(new Approval(TicketType.CHANGE, 1L, "APPROVER")));
        assertThatThrownBy(() -> service.decideApproval(1L, new ChangeApprovalRequest(ChangeApprovalDecision.REJECT, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REJECT_REASON_REQUIRED));
    }

    @Test
    void decideApprovalAlreadyDecidedRejected() {
        login("APPROVER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        Approval decided = new Approval(TicketType.CHANGE, 1L, "APPROVER");
        decided.approve(2L, "이미 승인");
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.of(decided));
        assertThatThrownBy(() -> service.decideApproval(1L, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_ALREADY_DECIDED));
    }

    @Test
    void decideApprovalWithoutApproverRoleForbidden() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L))
                .thenReturn(Optional.of(new Approval(TicketType.CHANGE, 1L, "APPROVER")));
        assertThatThrownBy(() -> service.decideApproval(1L, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void decideApprovalNotFound() {
        login("APPROVER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.decideApproval(1L, new ChangeApprovalRequest(ChangeApprovalDecision.APPROVE, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_NOT_FOUND));
    }

    // ---------- pending approvals ----------

    @Test
    void pendingApprovalsReturnsMatchingRole() {
        login("APPROVER");
        when(appUserRepository.findById(any())).thenReturn(Optional.empty());
        Approval pending = new Approval(TicketType.CHANGE, 1L, "APPROVER");
        when(approvalRepository.findByTicketTypeAndStatusAndApproverRoleIn(TicketType.CHANGE, ApprovalStatus.PENDING, List.of("APPROVER")))
                .thenReturn(List.of(pending));
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        var result = service.pendingApprovals();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).changeId()).isEqualTo(1L);
    }

    // ---------- result ----------

    @Test
    void recordResultBeforeApprovalRejected() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL, ApprovalRoute.CAB)));
        when(approvalRepository.findByTicketTypeAndTicketId(TicketType.CHANGE, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.recordResult(1L, new ResultRequest(Outcome.SUCCESS, false, "완료")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.CHANGE_NOT_APPROVED));
    }

    @Test
    void recordResultAfterApprovalSucceeds() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.IMPLEMENTATION, ApprovalRoute.AUTO)));
        var response = service.recordResult(1L, new ResultRequest(Outcome.SUCCESS, false, "정상 배포"));
        assertThat(response.outcome()).isEqualTo("SUCCESS");
    }

    // ---------- links ----------

    @Test
    void linkIncidentSuccess() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        Incident inc = new Incident("INC-2026-0001", "장애", "설명", Severity.SEV1, "svc", "prod");
        when(incidentRepository.findById(5L)).thenReturn(Optional.of(inc));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        var response = service.link(1L, new LinkRequest(LinkTargetType.INCIDENT, 5L));
        assertThat(response.targetType()).isEqualTo("INCIDENT");
        verify(ticketLinkRepository, times(2)).save(any());
    }

    @Test
    void linkProblemNotFound() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        when(problemRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.link(1L, new LinkRequest(LinkTargetType.PROBLEM, 7L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void linkProblemSuccess() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        Problem problem = new Problem("PRB-2026-0001", "요약", "설명", ProblemOrigin.REACTIVE, null, null, null, null);
        when(problemRepository.findById(7L)).thenReturn(Optional.of(problem));
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);
        var response = service.link(1L, new LinkRequest(LinkTargetType.PROBLEM, 7L));
        assertThat(response.targetType()).isEqualTo("PROBLEM");
    }

    // ---------- templates / schedule / metrics ----------

    @Test
    void listTemplatesReturnsActiveOnly() {
        login("CHANGE_MANAGER");
        when(templateRepository.findActive()).thenReturn(List.of(new com.itsm.change.domain.ChangeTemplate("표준 패치", "설명")));
        assertThat(service.listTemplates()).hasSize(1);
    }

    @Test
    void scheduleReturnsEmptyWhenNoneScheduled() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findSchedule(any(), any(), any())).thenReturn(List.of());
        List<ScheduleItemResponse> result = service.schedule(null, null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void metricsEmptyReturnsZero() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());
        var metrics = service.metrics(null, null);
        assertThat(metrics.total()).isEqualTo(0);
        assertThat(metrics.successRate()).isEqualTo(0);
    }

    @Test
    void metricsComputesRates() {
        login("CHANGE_MANAGER");
        ChangeRequest success = change(ChangeStatus.CLOSED, ApprovalRoute.AUTO);
        success.recordResult(Outcome.SUCCESS, false, null);
        ChangeRequest failure = change(ChangeStatus.CLOSED, ApprovalRoute.AUTO);
        failure.recordResult(Outcome.FAILURE, true, null);
        when(changeRequestRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(success, failure));
        var metrics = service.metrics(null, null);
        assertThat(metrics.total()).isEqualTo(2);
        assertThat(metrics.successRate()).isEqualTo(50.0);
        assertThat(metrics.failureRate()).isEqualTo(50.0);
    }

    // ---------- 문제→변경 연계(API-PRB-009) 재사용 ----------

    @Test
    void createLinkedChangeCreatesRfc() {
        service.createLinkedChange("문제 해결 변경", "설명");
        var captor = org.mockito.ArgumentCaptor.forClass(ChangeRequest.class);
        verify(changeRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getSummary()).isEqualTo("문제 해결 변경");
        assertThat(captor.getValue().getType()).isEqualTo(ChangeType.NORMAL);
        assertThat(captor.getValue().getApprovalRoute()).isEqualTo(ApprovalRoute.CAB);
    }

    @Test
    void existsChangeReflectsRepository() {
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED, ApprovalRoute.CAB)));
        assertThat(service.existsChange(1L)).isTrue();
        assertThat(service.existsChange(999L)).isFalse();
    }
}
