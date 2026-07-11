package com.itsm.change.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.application.dto.ClassificationRequest;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.application.dto.LinkRequest;
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.ScheduleItemResponse;
import com.itsm.change.application.dto.StatusTransitionRequest;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.LinkTargetType;
import com.itsm.change.domain.Outcome;
import com.itsm.change.domain.repository.ChangeAffectedSystemRepository;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.change.domain.repository.ChangeTemplateRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.compliance.domain.repository.ComplianceRequirementRepository;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangeServiceTest {

    @Mock ChangeRequestRepository changeRequestRepository;
    @Mock ChangeTemplateRepository templateRepository;
    @Mock ChangeAffectedSystemRepository affectedSystemRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock ProblemRepository problemRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock AssetService assetService;
    @Mock ComplianceRequirementRepository complianceRequirementRepository;
    @Mock ApprovalRequestRepository approvalRequestRepository;
    @Mock ApprovalGateService approvalGateService;

    ChangeService service;

    @BeforeEach
    void setUp() {
        service = new ChangeService(changeRequestRepository, templateRepository, affectedSystemRepository,
                ticketLinkRepository, timelineRepository, incidentRepository,
                problemRepository, assetService, complianceRequirementRepository, approvalRequestRepository,
                approvalGateService, appUserRepository);
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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

    private ChangeRequest change(ChangeStatus status) {
        ChangeRequest c = new ChangeRequest("CHG-2026-0001", "요약", "설명", ChangeType.NORMAL, ChangeRisk.HIGH,
                "구현계획", "롤백계획", null, null);
        if (status != ChangeStatus.REQUESTED) {
            c.changeStatus(status);
        }
        return c;
    }

    // ---------- create ----------

    @Test
    void createSuccess() {
        login("CHANGE_MANAGER");
        var response = service.create(new CreateChangeRequest("요약", "설명", ChangeType.NORMAL, null,
                "계획", List.of("sys-a"), "롤백", null, null));
        assertThat(response.ticketKey()).startsWith("CHG-");
        assertThat(response.status()).isEqualTo("REQUESTED");
        verify(affectedSystemRepository).save(any());
    }

    @Test
    void createStandardWithTemplateSuccess() {
        login("CHANGE_MANAGER");
        when(templateRepository.findById(1L)).thenReturn(Optional.of(new com.itsm.change.domain.ChangeTemplate("표준 패치", "설명")));
        var response = service.create(new CreateChangeRequest("표준 배포", null, ChangeType.STANDARD, ChangeRisk.LOW,
                null, null, null, null, 1L));
        assertThat(response.type()).isEqualTo("STANDARD");
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
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());
        var detail = service.detail(1L);
        assertThat(detail.status()).isEqualTo("REQUESTED");
    }

    // ---------- status transition ----------

    @Test
    void transitionSequential() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        var response = service.transition(1L, new StatusTransitionRequest(ChangeStatus.REVIEW, "검토"));
        assertThat(response.status()).isEqualTo("REVIEW");
    }

    @Test
    void transitionOutOfOrderRejected() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionToImplementationGatePassSucceeds() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL)));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any());
        var response = service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null));
        assertThat(response.status()).isEqualTo("IMPLEMENTATION");
    }

    @Test
    void transitionToImplementationGateBlockedPropagates409() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.APPROVAL)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(ChangeStatus.IMPLEMENTATION, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- classification ----------

    @Test
    void classifyUpdatesTypeAndRisk() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        var response = service.classify(1L, new ClassificationRequest(ChangeType.NORMAL, ChangeRisk.MEDIUM));
        assertThat(response.type()).isEqualTo("NORMAL");
        assertThat(response.risk()).isEqualTo("MEDIUM");
    }

    // ---------- result ----------

    @Test
    void recordResultSucceeds() {
        // Stage 1: 승인 완료 여부와 무관하게 기록 가능(게이트 연동은 Stage 2에서 완료).
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.IMPLEMENTATION)));
        var response = service.recordResult(1L, new ResultRequest(Outcome.SUCCESS, false, "정상 배포"));
        assertThat(response.outcome()).isEqualTo("SUCCESS");
    }

    // ---------- links ----------

    @Test
    void linkIncidentSuccess() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
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
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        when(problemRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.link(1L, new LinkRequest(LinkTargetType.PROBLEM, 7L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void linkProblemSuccess() {
        login("CHANGE_MANAGER");
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
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
        ChangeRequest success = change(ChangeStatus.CLOSED);
        success.recordResult(Outcome.SUCCESS, false, null);
        ChangeRequest failure = change(ChangeStatus.CLOSED);
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
    }

    @Test
    void existsChangeReflectsRepository() {
        when(changeRequestRepository.findById(1L)).thenReturn(Optional.of(change(ChangeStatus.REQUESTED)));
        assertThat(service.existsChange(1L)).isTrue();
        assertThat(service.existsChange(999L)).isFalse();
    }
}
