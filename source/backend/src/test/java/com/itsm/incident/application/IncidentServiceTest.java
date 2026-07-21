package com.itsm.incident.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.application.TicketCreationGateSupport;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.incident.application.dto.AssignRoleRequest;
import com.itsm.incident.application.dto.CreateIncidentRequest;
import com.itsm.incident.application.dto.EscalateRequest;
import com.itsm.incident.application.dto.PostmortemRequest;
import com.itsm.incident.application.dto.ResolveRequest;
import com.itsm.incident.application.dto.SeverityChangeRequest;
import com.itsm.incident.application.dto.StatusTransitionRequest;
import com.itsm.incident.application.dto.TimelineUpdateRequest;
import com.itsm.incident.domain.EscalationType;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Priority;
import com.itsm.incident.domain.ResponseRole;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.incident.domain.repository.IncidentResponderRepository;
import com.itsm.incident.domain.repository.IncidentSeverityHistoryRepository;
import com.itsm.incident.domain.repository.PostmortemActionItemRepository;
import com.itsm.incident.domain.repository.PostmortemFiveWhyRepository;
import com.itsm.incident.domain.repository.PostmortemRepository;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncidentServiceTest {

    @Mock IncidentRepository incidentRepository;
    @Mock IncidentResponderRepository responderRepository;
    @Mock IncidentSeverityHistoryRepository severityHistoryRepository;
    @Mock PostmortemRepository postmortemRepository;
    @Mock PostmortemFiveWhyRepository fiveWhyRepository;
    @Mock PostmortemActionItemRepository actionItemRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock TicketLinkRepository ticketLinkRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock com.itsm.problem.application.ProblemService problemService;
    @Mock com.itsm.change.application.ChangeService changeService;
    @Mock AssetService assetService;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;
    @Mock TicketCreationGateSupport ticketCreationGateSupport;

    IncidentService service;

    @BeforeEach
    void setUp() {
        service = new IncidentService(incidentRepository, responderRepository, severityHistoryRepository,
                postmortemRepository, fiveWhyRepository, actionItemRepository, timelineRepository,
                ticketLinkRepository, appUserRepository, problemService, changeService, assetService,
                approvalGateService, approvalRequestRepository, ticketCreationGateSupport);
        when(ticketCreationGateSupport.createThenGate(any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> ((java.util.function.Supplier<?>) inv.getArgument(0)).get());
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(responderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(severityHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(postmortemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fiveWhyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(actionItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(responderRepository.findByIncidentId(any())).thenReturn(List.of());
        when(ticketLinkRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(List.of());
        when(timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(any(), any())).thenReturn(List.of());
        when(fiveWhyRepository.findByPostmortemIdOrderByStepNoAsc(any())).thenReturn(List.of());
        when(actionItemRepository.findByPostmortemId(any())).thenReturn(List.of());
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(any(), any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Incident incident(Severity severity, IncidentStatus status) {
        Incident inc = new Incident("INC-2026-0001", "요약", "설명", severity, "svc", "prod");
        if (status != IncidentStatus.NEW) {
            inc.changeStatus(status);
        }
        return inc;
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    // ---------- create ----------

    @Test
    void createSuccess() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);

        var response = service.create(new CreateIncidentRequest("장애 요약", "설명", Severity.SEV1, "svc", "prod"));

        assertThat(response.ticketKey()).startsWith("INC-");
        assertThat(response.status()).isEqualTo("NEW");
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INCIDENT_NOT_FOUND));
    }

    @Test
    void detailSuccess() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));

        var response = service.detail(1L);

        assertThat(response.status()).isEqualTo("NEW");
        assertThat(response.severity()).isEqualTo("SEV2");
        assertThat(response.allowedTransitions()).containsExactly("IN_PROGRESS");
    }

    @Test
    void detailLinksExposeTargetTicketKeyNotRawId() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        var link = new com.itsm.common.ticket.TicketLink(
                com.itsm.common.ticket.TicketType.INCIDENT, 1L, com.itsm.common.ticket.TicketType.PROBLEM, 23L, "RELATED");
        when(ticketLinkRepository.findBySourceTypeAndSourceId(com.itsm.common.ticket.TicketType.INCIDENT, 1L))
                .thenReturn(List.of(link));
        when(problemService.ticketKeyOf(23L)).thenReturn("PRB-2026-0023");

        var response = service.detail(1L);

        assertThat(response.links()).hasSize(1);
        assertThat(response.links().get(0).type()).isEqualTo("PROBLEM");
        assertThat(response.links().get(0).targetKey()).isEqualTo("PRB-2026-0023");
    }

    @Test
    void detailLinksExposeChangeTargetTicketKey() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        var link = new com.itsm.common.ticket.TicketLink(
                com.itsm.common.ticket.TicketType.INCIDENT, 1L, com.itsm.common.ticket.TicketType.CHANGE, 7L, "RELATED");
        when(ticketLinkRepository.findBySourceTypeAndSourceId(com.itsm.common.ticket.TicketType.INCIDENT, 1L))
                .thenReturn(List.of(link));
        when(changeService.ticketKeyOf(7L)).thenReturn("CHG-2026-0007");

        var response = service.detail(1L);

        assertThat(response.links()).hasSize(1);
        assertThat(response.links().get(0).type()).isEqualTo("CHANGE");
        assertThat(response.links().get(0).targetKey()).isEqualTo("CHG-2026-0007");
    }

    // ---------- severity ----------

    @Test
    void changeSeverityForbiddenForNonManager() {
        login(1L, "SERVICE_DESK_AGENT");

        assertThatThrownBy(() -> service.changeSeverity(1L, new SeverityChangeRequest(Severity.SEV1, Priority.P1)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void changeSeverityNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeSeverity(9L, new SeverityChangeRequest(Severity.SEV1, Priority.P1)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INCIDENT_NOT_FOUND));
    }

    @Test
    void changeSeveritySuccessRecordsHistory() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV3, IncidentStatus.NEW)));

        var response = service.changeSeverity(1L, new SeverityChangeRequest(Severity.SEV1, Priority.P2));

        assertThat(response.severity()).isEqualTo("SEV1");
        assertThat(response.priority()).isEqualTo("P2");
    }

    // ---------- transition ----------

    @Test
    void transitionForbiddenForEndUser() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(IncidentStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void transitionInvalidThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(IncidentStatus.CLOSED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionFromClosedThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.CLOSED)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(IncidentStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionSuccess() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));

        var response = service.transition(1L, new StatusTransitionRequest(IncidentStatus.IN_PROGRESS, "대응 착수"));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void transitionToResolvedGatePassSucceeds() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.IN_PROGRESS)));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any(), any());

        var response = service.transition(1L, new StatusTransitionRequest(IncidentStatus.RESOLVED, null));

        assertThat(response.status()).isEqualTo("RESOLVED");
    }

    @Test
    void transitionToResolvedGateBlockedPropagates409() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.IN_PROGRESS)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(IncidentStatus.RESOLVED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- role assignment ----------

    @Test
    void assignRoleForbiddenForAgent() {
        login(1L, "SERVICE_DESK_AGENT");

        assertThatThrownBy(() -> service.assignRole(1L, new AssignRoleRequest(2L, ResponseRole.TECH_LEAD)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void assignRoleForbiddenForEndUser() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.assignRole(1L, new AssignRoleRequest(2L, ResponseRole.TECH_LEAD)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void assignRoleIncidentNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(9L, new AssignRoleRequest(2L, ResponseRole.TECH_LEAD)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INCIDENT_NOT_FOUND));
    }

    @Test
    void assignRoleUserNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(1L, new AssignRoleRequest(2L, ResponseRole.TECH_LEAD)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ASSIGNEE_NOT_FOUND));
    }

    @Test
    void assignRoleSuccess() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(mock(AppUser.class)));
        when(responderRepository.existsByIncidentIdAndUserIdAndResponseRole(1L, 2L, ResponseRole.TECH_LEAD))
                .thenReturn(false);

        var response = service.assignRole(1L, new AssignRoleRequest(2L, ResponseRole.TECH_LEAD));

        assertThat(response).isNotNull();
    }

    // ---------- escalation ----------

    @Test
    void escalateForbiddenForEndUser() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.escalate(1L, new EscalateRequest(2L, EscalationType.HIERARCHICAL, "이유")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void escalateTargetNotFoundThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.escalate(1L, new EscalateRequest(2L, EscalationType.HIERARCHICAL, "이유")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESCALATION_TARGET_NOT_FOUND));
    }

    @Test
    void escalateIncidentNotFoundThrows() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.escalate(9L, new EscalateRequest(2L, EscalationType.FUNCTIONAL, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INCIDENT_NOT_FOUND));
    }

    @Test
    void escalateSuccess() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(mock(AppUser.class)));

        var response = service.escalate(1L, new EscalateRequest(2L, EscalationType.HIERARCHICAL, "상위 대응 필요"));

        assertThat(response.targetUserId()).isEqualTo(2L);
        assertThat(response.type()).isEqualTo("HIERARCHICAL");
    }

    // ---------- timeline update ----------

    @Test
    void addUpdateForbiddenForEndUser() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.addUpdate(1L, new TimelineUpdateRequest("내용", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void addUpdateSuccess() {
        login(1L, "SERVICE_DESK_AGENT");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));

        var response = service.addUpdate(1L, new TimelineUpdateRequest("외부 공지", com.itsm.common.ticket.Visibility.EXTERNAL));

        assertThat(response).isNotNull();
    }

    // ---------- resolve / metrics ----------

    @Test
    void resolveForbiddenForAgent() {
        login(1L, "SERVICE_DESK_AGENT");

        assertThatThrownBy(() -> service.resolve(1L, new ResolveRequest(null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void resolveNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve(9L, new ResolveRequest(null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INCIDENT_NOT_FOUND));
    }

    @Test
    void resolveComputesAllMetrics() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.IN_PROGRESS)));
        OffsetDateTime start = OffsetDateTime.parse("2026-07-09T10:00:00Z");

        var response = service.resolve(1L, new ResolveRequest(
                start, start.plusMinutes(5), start.plusMinutes(30), "복구 완료"));

        assertThat(response.status()).isEqualTo("RESOLVED");
        assertThat(response.metrics().mttdMinutes()).isEqualTo(5);
        assertThat(response.metrics().mttaMinutes()).isEqualTo(25);
        assertThat(response.metrics().mttrMinutes()).isEqualTo(30);
    }

    @Test
    void resolveMissingImpactStartLeavesMetricsNull() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.IN_PROGRESS)));
        OffsetDateTime detected = OffsetDateTime.parse("2026-07-09T10:05:00Z");

        var response = service.resolve(1L, new ResolveRequest(
                null, detected, detected.plusMinutes(20), "복구"));

        assertThat(response.metrics().mttdMinutes()).isNull();
        assertThat(response.metrics().mttaMinutes()).isEqualTo(20);
        assertThat(response.metrics().mttrMinutes()).isNull();
    }

    @Test
    void resolveGatePassSucceeds() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.IN_PROGRESS)));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any(), any());

        var response = service.resolve(1L, new ResolveRequest(null, null, null, "복구 완료"));

        assertThat(response.status()).isEqualTo("RESOLVED");
    }

    @Test
    void resolveGateBlockedPropagates409() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.IN_PROGRESS)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.resolve(1L, new ResolveRequest(null, null, null, "복구 완료")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- postmortem ----------

    @Test
    void getPostmortemNotFoundThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.RESOLVED)));
        when(postmortemRepository.findByIncidentId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPostmortem(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.POSTMORTEM_NOT_FOUND));
    }

    @Test
    void savePostmortemForbiddenForAgent() {
        login(1L, "SERVICE_DESK_AGENT");

        assertThatThrownBy(() -> service.savePostmortem(1L,
                new PostmortemRequest("s", "t", List.of("why"), "근본원인", List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void savePostmortemRootCauseMissingThrows() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.RESOLVED)));

        assertThatThrownBy(() -> service.savePostmortem(1L,
                new PostmortemRequest("s", "t", List.of("why"), "  ", List.of())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ROOT_CAUSE_REQUIRED));
    }

    @Test
    void savePostmortemCreateSuccess() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV1, IncidentStatus.RESOLVED)));
        when(postmortemRepository.findByIncidentId(1L)).thenReturn(Optional.empty());

        var response = service.savePostmortem(1L,
                new PostmortemRequest("요약", "타임라인", List.of("why1", "why2"), "설정 오류", List.of()));

        assertThat(response.incidentId()).isEqualTo(1L);
        assertThat(response.rootCause()).isEqualTo("설정 오류");
    }

    // ---------- link (problem 도메인 연계) ----------

    @Test
    void linkExistingProblem() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(problemService.existsProblem(5L)).thenReturn(true);
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        var response = service.linkProblem(1L, new com.itsm.incident.application.dto.LinkProblemRequest(5L, false));

        assertThat(response.incidentId()).isEqualTo(1L);
        assertThat(response.problemId()).isEqualTo(5L);
        org.mockito.Mockito.verify(problemService).existsProblem(5L);
        // 양방향 링크(INCIDENT→PROBLEM, PROBLEM→INCIDENT) 2건 저장
        org.mockito.Mockito.verify(ticketLinkRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void linkNonExistentProblemFails() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(problemService.existsProblem(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.linkProblem(1L, new com.itsm.incident.application.dto.LinkProblemRequest(999L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_NOT_FOUND));
    }

    @Test
    void linkNewProblemCreatesReactiveProblem() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));
        when(problemService.createReactiveProblem(any(), any())).thenReturn(99L);
        when(ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(any(), any(), any(), any()))
                .thenReturn(false);

        var response = service.linkProblem(1L, new com.itsm.incident.application.dto.LinkProblemRequest(null, true));

        assertThat(response.problemId()).isEqualTo(99L);
    }

    @Test
    void linkProblemWithoutTargetFails() {
        login(1L, "INCIDENT_MANAGER");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident(Severity.SEV2, IncidentStatus.NEW)));

        assertThatThrownBy(() -> service.linkProblem(1L, new com.itsm.incident.application.dto.LinkProblemRequest(null, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.LINK_TARGET_REQUIRED));
    }

    @Test
    void linkProblemForbiddenForAgent() {
        login(1L, "SERVICE_DESK_AGENT");

        assertThatThrownBy(() -> service.linkProblem(1L, new com.itsm.incident.application.dto.LinkProblemRequest(5L, false)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- metrics ----------

    @Test
    void metricsAggregates() {
        login(1L, "INCIDENT_MANAGER");
        Incident a = incident(Severity.SEV1, IncidentStatus.RESOLVED);
        a.resolve(null, null, null, OffsetDateTime.now(), 5, 25, 30);
        Incident b = incident(Severity.SEV2, IncidentStatus.RESOLVED);
        b.resolve(null, null, null, OffsetDateTime.now(), 2, 8, 10);
        when(incidentRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(a, b));

        var response = service.metrics(null, null);

        assertThat(response.count()).isEqualTo(2);
        assertThat(response.severityDistribution().SEV1()).isEqualTo(1);
        assertThat(response.severityDistribution().SEV2()).isEqualTo(1);
        assertThat(response.avgMttrMinutes()).isEqualTo(20.0);
    }
}
